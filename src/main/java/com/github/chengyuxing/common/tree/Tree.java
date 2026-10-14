package com.github.chengyuxing.common.tree;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Tree data operations, ported from rabbit-sql-doc-zh's TypeScript Tree.
 * Node IDs must be non-null and unique; parent relationships must be acyclic. Returned nodes and
 * root/sibling lists belong to this mutable tree. This class is not thread-safe.
 *
 * @param <T> concrete node type, retaining its own payload
 * @param <ID> node identifier type
 */
public class Tree<T extends TreeNode<T, ID>, ID> {
    private Map<ID, T> indexes = new LinkedHashMap<>();
    private List<T> treeNodes = new ArrayList<>();
    private final Function<T, T> nodeCopier;
    protected final List<T> data;

    /** Positions within a sibling list, which includes the current node. */
    public enum SiblingPosition {
        NEXT, PREVIOUS, FIRST, LAST
    }

    /**
     * Builds a tree from flat data using shallow field copies. Node classes must
     * have a no-argument constructor; inherited instance fields are copied too.
     *
     * @param data flat nodes; their existing parent/children references are ignored
     */
    public Tree(List<T> data) {
        this(data, Tree::shallowCopy);
    }

    /**
     * Builds a tree with an explicit copier, for example a copy constructor.
     * The copier must return a new node and is also used by {@link #toFlat()}.
     *
     * @param data flat nodes
     * @param nodeCopier function that copies a node's fields
     */
    public Tree(List<T> data, Function<T, T> nodeCopier) {
        this.data = Objects.requireNonNull(data, "data");
        this.nodeCopier = Objects.requireNonNull(nodeCopier, "nodeCopier");
        buildTree();
    }

    /** Rebuilds the tree from shallow copies of the original flat data. */
    protected void buildTree() {
        Map<ID, T> idMap = new LinkedHashMap<>();
        List<T> roots = new ArrayList<>();
        for (T source : data) {
            requireId(source);
            T node = copyNode(source);
            ID id = requireId(node);
            if (idMap.containsKey(id)) {
                throw new IllegalArgumentException("Duplicate node ID: " + id);
            }
            node.setChildren(new ArrayList<>());
            node.setParent(null);
            idMap.put(id, node);
        }
        validateParentCycles(idMap);
        for (T node : idMap.values()) {
            if (node.getPid() == null) {
                roots.add(node);
                continue;
            }
            T parent = idMap.get(node.getPid());
            if (parent != null) {
                node.setParent(parent);
                parent.getChildren().add(node);
            }
        }
        indexes = idMap;
        treeNodes = roots;
    }

    /** @return the live root list */
    public List<T> getRoots() {
        return treeNodes;
    }

    /** @return whether there are no roots, even if orphan nodes are indexed */
    public boolean isEmpty() {
        return treeNodes.isEmpty();
    }

    /** @return number of roots */
    public int getRootCount() {
        return treeNodes.size();
    }

    /** @return number of indexed IDs, including orphans */
    public int getSize() {
        return indexes.size();
    }

    /**
     * @param id node ID
     * @return the indexed node, or null if absent
     */
    public T getNode(ID id) {
        return indexes.get(id);
    }

    /**
     * @param id node ID
     * @return whether the node exists and has children
     */
    public boolean hasChildren(ID id) {
        T node = getNode(id);
        return node != null && !node.getChildren().isEmpty();
    }

    /**
     * @param predicate node predicate, evaluated in index insertion order
     * @return matching IDs, including indexed orphans
     */
    public List<ID> search(Predicate<T> predicate) {
        List<ID> result = new ArrayList<>();
        indexes.forEach((id, node) -> {
            if (predicate.test(node)) {
                result.add(id);
            }
        });
        return result;
    }

    /**
     * @param predicate node predicate
     * @return matching IDs followed by their ancestors and descendants, with
     * duplicates removed in first-occurrence order
     */
    public List<ID> searchWithContext(Predicate<T> predicate) {
        Set<ID> result = new LinkedHashSet<>();
        for (ID id : search(predicate)) {
            result.add(id);
            getAncestors(id).forEach(node -> result.add(node.getId()));
            getDescendants(id).forEach(node -> result.add(node.getId()));
        }
        return new ArrayList<>(result);
    }

    /**
     * Adds the supplied node and its subtree after validating the entire addition.
     * The supplied children define the subtree; existing pid/parent values are replaced.
     *
     * @param pid parent ID; null adds a root
     * @param node node or subtree to add, mutated in place after validation
     * @throws IllegalArgumentException if the parent is absent, an ID is null or duplicated,
     *                                  or the subtree contains a cycle
     */
    public void addNode(ID pid, T node) {
        T parent = pid == null ? null : getNode(pid);
        if (pid != null && parent == null) {
            throw new IllegalArgumentException("Parent node not found: " + pid);
        }
        requireId(node);
        Map<ID, T> additions = new LinkedHashMap<>();
        Map<T, T> parents = new IdentityHashMap<>();
        Deque<T> pending = new ArrayDeque<>();
        parents.put(node, parent);
        pending.addFirst(node);
        while (!pending.isEmpty()) {
            T current = pending.removeFirst();
            ID id = requireId(current);
            if (indexes.containsKey(id) || additions.containsKey(id)) {
                throw new IllegalArgumentException("Duplicate node ID or cycle in subtree: " + id);
            }
            additions.put(id, current);
            List<T> children = current.getChildren();
            if (children != null) {
                for (ListIterator<T> it = children.listIterator(children.size()); it.hasPrevious();) {
                    T child = it.previous();
                    requireId(child);
                    parents.put(child, current);
                    pending.addFirst(child);
                }
            }
        }
        additions.values().forEach(current -> current.setChildren(new ArrayList<>()));
        additions.forEach((id, current) -> {
            T owner = parents.get(current);
            current.setPid(owner == null ? null : owner.getId());
            current.setParent(owner);
            (owner == null ? treeNodes : owner.getChildren()).add(current);
            indexes.put(id, current);
        });
    }

    /**
     * Moves a node and its subtree, appending it to the new parent's children or roots.
     * Moving to the current parent leaves sibling order unchanged.
     *
     * @param id node ID
     * @param pid new parent ID; null moves to the roots
     * @throws IllegalArgumentException if either node is absent or the move creates a cycle
     */
    public void moveNode(ID id, ID pid) {
        T node = getNode(id);
        if (node == null) {
            throw new IllegalArgumentException("Node not found: " + id);
        }
        T parent = pid == null ? null : getNode(pid);
        if (pid != null && parent == null) {
            throw new IllegalArgumentException("Parent node not found: " + pid);
        }
        Set<ID> visited = new HashSet<>();
        for (T current = parent; current != null; current = current.getParent()) {
            if (current == node || !visited.add(current.getId())) {
                throw new IllegalArgumentException("Move would create a cycle: " + id + " -> " + pid);
            }
        }
        if (node.getParent() == parent && Objects.equals(node.getPid(), pid)) {
            return;
        }
        detachNode(node);
        node.setPid(pid);
        node.setParent(parent);
        (parent == null ? treeNodes : parent.getChildren()).add(node);
    }

    /**
     * Updates business fields. ID, parent ID and structural references are protected;
     * use {@link #moveNode(Object, Object)} to change the parent.
     *
     * @param id indexed node ID; absent nodes are ignored
     * @param updates field names and values
     * @throws IllegalArgumentException if a field is unknown, incompatible or structural
     */
    public void updateNode(ID id, Map<String, ?> updates) {
        T node = getNode(id);
        if (node == null) {
            return;
        }
        for (String name : updates.keySet()) {
            if ("id".equals(name) || "pid".equals(name) || "children".equals(name) || "parent".equals(name)) {
                throw new IllegalArgumentException("Cannot update tree field: " + name);
            }
        }
        ID originalId = node.getId();
        ID originalPid = node.getPid();
        T originalParent = node.getParent();
        List<T> originalChildren = node.getChildren();
        updates.forEach((name, value) -> {
            Field field = getField(node.getClass(), name);
            try {
                field.setAccessible(true);
                Object previous = field.get(node);
                field.set(node, value);
                if (!Objects.equals(originalId, node.getId()) || !Objects.equals(originalPid, node.getPid())
                        || originalParent != node.getParent() || originalChildren != node.getChildren()) {
                    field.set(node, previous);
                    throw new IllegalArgumentException("Cannot update tree structure through field: " + name);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Cannot update node field: " + name, e);
            }
        });
    }

    /**
     * Removes a node from the root hierarchy and removes its subtree IDs from the index.
     *
     * @param id node ID
     */
    public void removeNode(ID id) {
        T node = getNode(id);
        if (node == null) {
            return;
        }
        List<T> descendants = getDescendants(id);
        detachNode(node);
        indexes.remove(id);
        descendants.forEach(child -> indexes.remove(child.getId()));
    }

    /**
     * @return shallow copies in root-first depth-first order, excluding orphans,
     * with parent and children set to null (Java's absent optional properties)
     */
    public List<T> toFlat() {
        List<T> result = new ArrayList<>();
        for (T node : traverse(treeNodes)) {
            T copy = copyNode(node);
            copy.setChildren(null);
            copy.setParent(null);
            result.add(copy);
        }
        return result;
    }

    /**
     * Returns a shallow node copy of the tree retaining matches and their ancestors.
     * Matching a parent does not automatically retain its descendants. Root and sibling
     * order is preserved, including after moves. Matching orphans remain indexed orphans.
     * Payload objects follow the configured copier's shallow-copy semantics.
     *
     * @param predicate predicate evaluated once per indexed node, in index order
     * @return an independent mutable tree, indexed in traversal order
     */
    public Tree<T, ID> filterTree(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        Set<ID> retained = new HashSet<>();
        for (T node : indexes.values()) {
            if (predicate.test(node)) {
                for (T current = node; current != null && retained.add(current.getId()); current = current.getParent()) {
                    // Retain each ancestor once, shared by any matching descendants.
                }
            }
        }
        List<T> forest = new ArrayList<>(treeNodes);
        for (T node : indexes.values()) {
            if (node.getParent() == null && node.getPid() != null) {
                forest.add(node);
            }
        }
        List<T> filtered = new ArrayList<>();
        for (T node : traverse(forest)) {
            if (retained.contains(node.getId())) {
                filtered.add(node);
            }
        }
        return new Tree<>(filtered, nodeCopier);
    }

    /**
     * @param id node ID
     * @return ancestors from the immediate parent up to the root
     */
    public List<T> getAncestors(ID id) {
        List<T> result = new ArrayList<>();
        T node = getNode(id);
        T parent = node == null ? null : node.getParent();
        while (parent != null) {
            result.add(parent);
            parent = parent.getParent();
        }
        return result;
    }

    /**
     * @param id node ID
     * @return live nodes from the root (or highest known orphan ancestor) to the node,
     *         including itself; an empty list if absent
     */
    public List<T> getPath(ID id) {
        T node = getNode(id);
        if (node == null) {
            return new ArrayList<>();
        }
        List<T> result = getAncestors(id);
        Collections.reverse(result);
        result.add(node);
        return result;
    }

    /**
     * @param id node ID
     * @return descendants in depth-first order, excluding the node itself
     */
    public List<T> getDescendants(ID id) {
        T node = getNode(id);
        return node == null ? new ArrayList<>() : traverse(node.getChildren());
    }

    /**
     * @param id node ID
     * @return the live sibling list, including the node; roots for parentless
     * nodes, or a new empty list if the node is absent
     */
    public List<T> getSiblings(ID id) {
        T node = getNode(id);
        if (node == null) {
            return new ArrayList<>();
        }
        return node.getParent() == null ? treeNodes : node.getParent().getChildren();
    }

    /**
     * @param id node ID
     * @param position position within the sibling list
     * @return sibling at the position, or null if absent/out of bounds
     */
    public T getSibling(ID id, SiblingPosition position) {
        T node = getNode(id);
        if (node == null) {
            return null;
        }
        List<T> siblings = getSiblings(id);
        int index = -1;
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i) == node) {
                index = i;
                break;
            }
        }
        switch (position) {
            case NEXT: index++; break;
            case PREVIOUS: index--; break;
            case FIRST: index = 0; break;
            case LAST: index = siblings.size() - 1; break;
            default: break;
        }
        return index < 0 || index >= siblings.size() ? null : siblings.get(index);
    }

    private ID requireId(T node) {
        if (node == null || node.getId() == null) {
            throw new IllegalArgumentException("Tree node and node ID must not be null");
        }
        return node.getId();
    }

    private void validateParentCycles(Map<ID, T> nodes) {
        Set<ID> checked = new HashSet<>();
        for (T node : nodes.values()) {
            Set<ID> path = new HashSet<>();
            for (T current = node; current != null && !checked.contains(current.getId());
                 current = nodes.get(current.getPid())) {
                if (!path.add(current.getId())) {
                    throw new IllegalArgumentException("Cycle detected at node: " + current.getId());
                }
            }
            checked.addAll(path);
        }
    }

    private void detachNode(T node) {
        List<T> siblings = node.getParent() == null ? treeNodes : node.getParent().getChildren();
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i) == node) {
                siblings.remove(i);
                return;
            }
        }
    }

    private List<T> traverse(List<T> roots) {
        List<T> result = new ArrayList<>();
        Deque<T> pending = new ArrayDeque<>(roots);
        while (!pending.isEmpty()) {
            T node = pending.removeFirst();
            result.add(node);
            List<T> children = node.getChildren();
            for (ListIterator<T> it = children.listIterator(children.size()); it.hasPrevious();) {
                pending.addFirst(it.previous());
            }
        }
        return result;
    }

    private T copyNode(T source) {
        T copy = Objects.requireNonNull(nodeCopier.apply(source), "Node copier returned null");
        if (copy == source) {
            throw new IllegalArgumentException("Node copier must return a new node");
        }
        return copy;
    }

    @SuppressWarnings("unchecked")
    private static <T> T shallowCopy(T source) {
        try {
            Constructor<?> constructor = source.getClass().getDeclaredConstructor();
            constructor.setAccessible(true);
            T copy = (T) constructor.newInstance();
            for (Class<?> type = source.getClass(); type != Object.class; type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
                        field.setAccessible(true);
                        field.set(copy, field.get(source));
                    }
                }
            }
            return copy;
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Cannot copy node; provide a node copier for classes without a no-arg constructor", e);
        }
    }

    private static Field getField(Class<?> type, String name) {
        for (Class<?> current = type; current != Object.class; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    break;
                }
                return field;
            } catch (NoSuchFieldException ignored) {
                // Payload fields may be inherited.
            }
        }
        throw new IllegalArgumentException("Unknown node field: " + name);
    }
}
