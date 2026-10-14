package tests;

import com.github.chengyuxing.common.tree.TreeNode;
import com.github.chengyuxing.common.tree.Tree;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.chengyuxing.common.tree.Tree.SiblingPosition.*;
import static org.junit.Assert.*;

public class TreeRegressionTest {
    static List<Node<Long>> fixture() {
        return Arrays.asList(new Node<>(3L, 2L, "leaf"), new Node<>(1L, null, "root-a"),
                new Node<>(2L, 1L, "branch"), new Node<>(4L, null, "root-b"),
                new Node<>(5L, 99L, "orphan"));
    }

    static <ID> List<ID> ids(List<Node<ID>> nodes) {
        return nodes.stream().map(Node::getId).collect(Collectors.toList());
    }

    @Test
    public void buildingCopiesInheritedPayloadWithoutChangingInputNodes() {
        Node<Long> input = new Node<>(1L, null, "root");
        input.payload.put("key", "value");
        Node<Long> previousParent = new Node<>(99L, null, "old-parent");
        List<Node<Long>> previousChildren = Collections.singletonList(new Node<>(9L, 1L, "old-child"));
        input.setParent(previousParent);
        input.setChildren(previousChildren);

        Tree<Node<Long>, Long> tree = new Tree<>(Collections.singletonList(input));
        Node<Long> root = tree.getNode(1L);
        assertNotSame(input, root);
        assertEquals("root", root.label);
        assertSame(input.payload, root.payload);
        assertNull(root.getParent());
        assertTrue(root.getChildren().isEmpty());
        assertSame(previousParent, input.getParent());
        assertSame(previousChildren, input.getChildren());
        root.label = "updated";
        assertEquals("root", input.label);
    }

    @Test
    public void rootsAndIndexOrderMatchTheTypescriptFixture() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        assertEquals(Arrays.asList(1L, 4L), ids(tree.getRoots()));
        assertEquals(2, tree.getRootCount());
        assertEquals(5, tree.getSize());
        assertFalse(tree.isEmpty());
        assertEquals(Arrays.asList(3L, 1L, 2L, 4L, 5L), tree.search(node -> true));
        assertTrue(tree.hasChildren(1L));
        assertTrue(tree.hasChildren(2L));
        assertFalse(tree.hasChildren(3L));
        assertFalse(tree.hasChildren(999L));
        assertSame(tree.getNode(1L), tree.getNode(2L).getParent());
        assertNull(tree.getNode(5L).getParent());
        assertEquals(Long.valueOf(99), tree.getNode(5L).getPid());
    }

    @Test
    public void contextSearchUsesIndexOrderAndDeduplicatesTheFirstOccurrence() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        assertEquals(Arrays.asList(3L, 2L, 1L),
                tree.searchWithContext(node -> node.getId() == 2L || node.getId() == 3L));
        assertEquals(Arrays.asList(2L, 1L, 3L), tree.searchWithContext(node -> node.getId() == 2L));
        assertEquals(Collections.singletonList(5L), tree.searchWithContext(node -> node.getId() == 5L));
        assertTrue(tree.searchWithContext(node -> false).isEmpty());
    }

    @Test
    public void subtreeAdditionMutatesSuppliedNodesAndIndexesEveryDescendant() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        Node<Long> root = new Node<>(6L, 99L, "added-root");
        Node<Long> child = new Node<>(7L, 99L, "added-child");
        Node<Long> grandchild = new Node<>(8L, 99L, "added-grandchild");
        root.setChildren(Collections.singletonList(child));
        child.setChildren(Collections.singletonList(grandchild));
        tree.addNode(null, root);
        Node<Long> addedLeaf = new Node<>(9L, null, "added-leaf");
        tree.addNode(2L, addedLeaf);

        assertSame(root, tree.getNode(6L));
        assertSame(child, tree.getNode(7L));
        assertSame(grandchild, tree.getNode(8L));
        assertSame(child, grandchild.getParent());
        assertEquals(Long.valueOf(7), grandchild.getPid());
        assertSame(tree.getNode(2L), addedLeaf.getParent());
        assertNull(root.getPid());
        assertNull(root.getParent());
        assertEquals(Arrays.asList(1L, 4L, 6L), ids(tree.getRoots()));
        assertEquals(Arrays.asList(7L, 6L), ids(tree.getAncestors(8L)));
        assertEquals(Arrays.asList(3L, 9L), ids(tree.getDescendants(2L)));
        assertEquals(Arrays.asList(1L, 2L, 3L, 9L, 4L, 6L, 7L, 8L), ids(tree.toFlat()));
        assertEquals(9, tree.getSize());
    }

    @Test
    public void missingParentsDoNotAddNodesAndOrphansDoNotBecomeRoots() {
        Tree<Node<Long>, Long> tree = new Tree<>(Collections.singletonList(new Node<>(5L, 99L, "orphan")));
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.getRootCount());
        assertEquals(1, tree.getSize());
        assertTrue(tree.toFlat().isEmpty());
        expectIllegal(() -> tree.addNode(99L, new Node<>(6L, null, "missing-parent")));
        assertNull(tree.getNode(6L));
        assertEquals(1, tree.getSize());
        tree.removeNode(5L);
        assertEquals(0, tree.getSize());
    }

    @Test
    public void partialUpdatesCopyPayloadFieldsButExcludeParentAndChildren() {
        List<Node<Long>> input = fixture();
        Tree<Node<Long>, Long> tree = new Tree<>(input);
        tree.updateNode(2L, Collections.singletonMap("label", "updated"));
        assertEquals("updated", tree.getNode(2L).label);
        assertEquals("branch", input.get(2).label);
        expectIllegal(() -> tree.updateNode(2L, Collections.singletonMap("children", Collections.emptyList())));
        expectIllegal(() -> tree.updateNode(2L, Collections.singletonMap("parent", null)));
        expectIllegal(() -> tree.updateNode(2L, Collections.singletonMap("unknown", "value")));
        expectIllegal(() -> tree.updateNode(2L, Collections.singletonMap("STATIC_VALUE", "value")));
        assertEquals(Collections.singletonList(3L), ids(tree.getNode(2L).getChildren()));
        assertSame(tree.getNode(1L), tree.getNode(2L).getParent());
        tree.updateNode(999L, Collections.singletonMap("label", "ignored"));
    }

    @Test
    public void identifierUpdatesAreRejectedBeforeChangingPayloadOrIndexes() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        Node<Long> node = tree.getNode(2L);
        Map<String, Object> updates = new LinkedHashMap<>();
        updates.put("label", "must remain unchanged");
        updates.put("id", 20L);
        expectIllegal(() -> tree.updateNode(2L, updates));
        expectIllegal(() -> tree.updateNode(2L, Collections.singletonMap("pid", 4L)));
        assertEquals("branch", node.label);
        assertSame(node, tree.getNode(2L));
        assertNull(tree.getNode(20L));
        assertEquals(Long.valueOf(2), node.getId());
        assertEquals(Long.valueOf(1), node.getPid());
        assertSame(tree.getNode(1L), node.getParent());
    }

    @Test
    public void removingBranchesAndRootsRemovesTheirDescendantIndexEntries() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        tree.addNode(2L, new Node<>(9L, null, "added-leaf"));
        tree.removeNode(2L);
        for (long id : new long[]{2, 3, 9}) {
            assertNull(tree.getNode(id));
        }
        assertFalse(tree.hasChildren(1L));
        assertEquals(Arrays.asList(1L, 4L, 5L), tree.search(node -> true));
        assertEquals(Arrays.asList(1L, 4L), ids(tree.toFlat()));
        tree.removeNode(1L);
        assertEquals(Collections.singletonList(4L), ids(tree.getRoots()));
        tree.removeNode(999L);
        assertEquals(2, tree.getSize());
    }

    @Test
    public void flatteningProducesCopiesWithoutParentOrChildrenAndExcludesOrphans() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        List<Node<Long>> flat = tree.toFlat();
        assertEquals(Arrays.asList(1L, 2L, 3L, 4L), ids(flat));
        for (Node<Long> node : flat) {
            assertNotSame(tree.getNode(node.getId()), node);
            assertNull(node.getChildren());
            assertNull(node.getParent());
            assertSame(tree.getNode(node.getId()).payload, node.payload);
        }
        flat.get(1).label = "changed";
        assertEquals("branch", tree.getNode(2L).label);
        assertEquals(Arrays.asList(2L, 1L), ids(tree.getAncestors(3L)));
        assertEquals(Arrays.asList(2L, 3L), ids(tree.getDescendants(1L)));
        assertTrue(tree.getAncestors(999L).isEmpty());
        assertTrue(tree.getDescendants(999L).isEmpty());
    }

    @Test
    public void siblingListsIncludeSelfAndPositionsMatchTheFrontend() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        assertSame(tree.getRoots(), tree.getSiblings(1L));
        assertEquals(Collections.singletonList(2L), ids(tree.getSiblings(2L)));
        assertSame(tree.getNode(4L), tree.getSibling(1L, NEXT));
        assertSame(tree.getNode(1L), tree.getSibling(4L, PREVIOUS));
        assertSame(tree.getNode(1L), tree.getSibling(4L, FIRST));
        assertSame(tree.getNode(4L), tree.getSibling(1L, LAST));
        assertNull(tree.getSibling(1L, PREVIOUS));
        assertNull(tree.getSibling(4L, NEXT));
        assertSame(tree.getNode(2L), tree.getSibling(2L, FIRST));
        assertNull(tree.getSibling(2L, NEXT));
        assertSame(tree.getRoots(), tree.getSiblings(5L));
        assertSame(tree.getNode(1L), tree.getSibling(5L, NEXT));
        assertTrue(tree.getSiblings(999L).isEmpty());
        assertNull(tree.getSibling(999L, FIRST));
    }

    @Test
    public void siblingPositionUsesIdentityEvenForNodesWithEqualPayloads() {
        Tree<Node<Long>, Long> tree = new Tree<>(Arrays.asList(
                new EqualNode(1L), new EqualNode(2L), new EqualNode(3L)));
        assertEquals(tree.getNode(1L), tree.getNode(2L));
        assertSame(tree.getNode(3L), tree.getSibling(2L, NEXT));
        assertSame(tree.getNode(1L), tree.getSibling(2L, PREVIOUS));
    }

    @Test
    public void nullPidAloneIdentifiesRootsAcrossDifferentIdTypes() {
        Tree<Node<String>, String> strings = new Tree<>(Arrays.asList(
                new Node<>("child", "", "child"), new Node<>("", null, "root")));
        assertEquals(Collections.singletonList(""), ids(strings.getRoots()));
        assertEquals(Collections.singletonList("child"), ids(strings.getDescendants("")));
        Tree<Node<Integer>, Integer> numbers = new Tree<>(Arrays.asList(
                new Node<>(1, 0, "child"), new Node<>(0, null, "root")));
        assertEquals(Collections.singletonList(0), ids(numbers.getRoots()));
        assertSame(numbers.getNode(0), numbers.getNode(1).getParent());
        Tree<Node<Long>, Long> empty = new Tree<>(Collections.<Node<Long>>emptyList());
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.getRootCount());
        assertEquals(0, empty.getSize());
    }

    @Test
    public void explicitCopiersSupportNodesWithoutNoArgConstructors() {
        NoDefaultNode input = new NoDefaultNode("root");
        expectIllegal(() -> new Tree<>(Collections.singletonList(input)));
        Tree<NoDefaultNode, String> tree = new Tree<>(Collections.singletonList(input), NoDefaultNode::new);
        assertNotSame(input, tree.getNode("root"));
        assertNotSame(tree.getNode("root"), tree.toFlat().get(0));
        expectIllegal(() -> new Tree<>(Collections.singletonList(input), node -> node));
        try {
            new Tree<>(Collections.singletonList(input), node -> null);
            fail("Expected null copier result to fail");
        } catch (NullPointerException expected) {
            assertEquals("Node copier returned null", expected.getMessage());
        }
    }

    private static void expectIllegal(Runnable action) {
        try {
            action.run();
            fail("Expected an invalid node operation to fail");
        } catch (IllegalArgumentException expected) {
            // Invalid copy contracts and excluded/unknown fields are rejected.
        }
    }

    static class Payload {
        static final String STATIC_VALUE = "constant";
        String label;
        Map<String, String> payload = new HashMap<>();
    }

    static class Node<ID> extends Payload implements TreeNode<Node<ID>, ID> {
        private ID id;
        private ID pid;
        private List<Node<ID>> children;
        private Node<ID> parent;

        private Node() {}

        Node(ID id, ID pid, String label) {
            this.id = id;
            this.pid = pid;
            this.label = label;
        }

        @Override public ID getId() { return id; }
        @Override public void setId(ID id) { this.id = id; }
        @Override public ID getPid() { return pid; }
        @Override public void setPid(ID pid) { this.pid = pid; }
        @Override public List<Node<ID>> getChildren() { return children; }
        @Override public void setChildren(List<Node<ID>> children) { this.children = children; }
        @Override public Node<ID> getParent() { return parent; }
        @Override public void setParent(Node<ID> parent) { this.parent = parent; }
    }

    static class EqualNode extends Node<Long> {
        private EqualNode() { super(null, null, "equal"); }
        EqualNode(Long id) { super(id, null, "equal"); }
        @Override public boolean equals(Object other) { return other instanceof EqualNode; }
        @Override public int hashCode() { return 1; }
    }

    static class NoDefaultNode implements TreeNode<NoDefaultNode, String> {
        private String id;
        private String pid;
        private List<NoDefaultNode> children;
        private NoDefaultNode parent;

        NoDefaultNode(String id) { this.id = id; }
        NoDefaultNode(NoDefaultNode node) { this.id = node.id; this.pid = node.pid; }
        @Override public String getId() { return id; }
        @Override public void setId(String id) { this.id = id; }
        @Override public String getPid() { return pid; }
        @Override public void setPid(String pid) { this.pid = pid; }
        @Override public List<NoDefaultNode> getChildren() { return children; }
        @Override public void setChildren(List<NoDefaultNode> children) { this.children = children; }
        @Override public NoDefaultNode getParent() { return parent; }
        @Override public void setParent(NoDefaultNode parent) { this.parent = parent; }
    }
}
