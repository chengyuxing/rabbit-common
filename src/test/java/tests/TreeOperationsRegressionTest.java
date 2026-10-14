package tests;

import com.github.chengyuxing.common.tree.Tree;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static tests.TreeRegressionTest.*;

public class TreeOperationsRegressionTest {
    @Test
    public void constructionRejectsDuplicateAndNullIdsWithoutChangingInputs() {
        Node<Long> first = new Node<>(1L, null, "first");
        List<Node<Long>> children = Collections.singletonList(new Node<>(7L, null, "untouched"));
        first.setChildren(children);
        invalid(() -> new Tree<>(Arrays.asList(first, new Node<>(1L, null, "duplicate"))));
        invalid(() -> new Tree<>(Collections.singletonList(new Node<Long>(null, null, "null-id"))));
        invalid(() -> new Tree<>(Collections.singletonList((Node<Long>) null)));
        assertSame(children, first.getChildren());
        assertNull(first.getParent());
    }

    @Test
    public void constructionRejectsSelfReferencesAndCyclesEvenWithoutRoots() {
        invalid(() -> new Tree<>(Collections.singletonList(new Node<>(1L, 1L, "self"))));
        invalid(() -> new Tree<>(Arrays.asList(new Node<>(1L, 2L, "a"), new Node<>(2L, 1L, "b"))));
        invalid(() -> new Tree<>(Arrays.asList(new Node<>(0L, null, "root"), new Node<>(1L, 2L, "a"),
                new Node<>(2L, 3L, "b"), new Node<>(3L, 1L, "c"))));
    }

    @Test
    public void missingParentDoesNotMutateTheSuppliedSubtree() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        Node<Long> node = new Node<>(6L, 777L, "new");
        Node<Long> previousParent = new Node<>(777L, null, "old");
        Node<Long> child = new Node<>(7L, 6L, "child");
        List<Node<Long>> children = Collections.singletonList(child);
        node.setChildren(children);
        node.setParent(previousParent);
        invalid(() -> tree.addNode(999L, node));
        assertSame(children, node.getChildren());
        assertSame(previousParent, node.getParent());
        assertEquals(Long.valueOf(777), node.getPid());
        assertNull(child.getParent());
        assertNull(tree.getNode(6L));
        assertEquals(5, tree.getSize());
    }

    @Test
    public void duplicatesInsideOrOutsideTheSubtreeAreRejectedBeforeAttachingAnything() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        Node<Long> root = new Node<>(6L, null, "new-root");
        Node<Long> existingId = new Node<>(3L, null, "duplicate");
        List<Node<Long>> children = Collections.singletonList(existingId);
        root.setChildren(children);
        invalid(() -> tree.addNode(1L, root));
        assertSame(children, root.getChildren());
        assertNull(root.getParent());
        assertNull(existingId.getParent());
        assertNull(tree.getNode(6L));
        assertEquals("leaf", tree.getNode(3L).label);
        Node<Long> a = new Node<>(7L, null, "a");
        Node<Long> b = new Node<>(7L, null, "b");
        root.setChildren(Arrays.asList(a, b));
        invalid(() -> tree.addNode(null, root));
        assertNull(tree.getNode(7L));
        assertEquals(5, tree.getSize());
    }

    @Test
    public void cyclicAndNullChildrenAreRejectedWithoutMutatingTheSubtree() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        Node<Long> root = new Node<>(6L, null, "root");
        Node<Long> child = new Node<>(7L, null, "child");
        List<Node<Long>> rootsChildren = Collections.singletonList(child);
        List<Node<Long>> childsChildren = Collections.singletonList(root);
        root.setChildren(rootsChildren);
        child.setChildren(childsChildren);
        invalid(() -> tree.addNode(null, root));
        assertSame(rootsChildren, root.getChildren());
        assertSame(childsChildren, child.getChildren());
        assertNull(root.getParent());
        assertNull(child.getParent());
        root.setChildren(Collections.singletonList(null));
        invalid(() -> tree.addNode(null, root));
        invalid(() -> tree.addNode(null, new Node<Long>(null, null, "null-id")));
        assertEquals(5, tree.getSize());
    }

    @Test
    public void movingABranchUpdatesParentsPathsAndKeepsIndexAndNodeIdentity() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        Node<Long> branch = tree.getNode(2L);
        Node<Long> leaf = tree.getNode(3L);
        List<Long> indexOrder = tree.search(node -> true);
        tree.moveNode(2L, 4L);
        assertSame(branch, tree.getNode(2L));
        assertSame(leaf, branch.getChildren().get(0));
        assertSame(branch, leaf.getParent());
        assertSame(tree.getNode(4L), branch.getParent());
        assertEquals(Long.valueOf(4), branch.getPid());
        assertFalse(tree.hasChildren(1L));
        assertEquals(Arrays.asList(4L, 2L, 3L), ids(tree.getPath(3L)));
        assertEquals(indexOrder, tree.search(node -> true));
        assertEquals(5, tree.getSize());
    }

    @Test
    public void movingBetweenRootsAndChildrenMaintainsTheWholeSubtree() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        tree.moveNode(2L, null);
        assertEquals(Arrays.asList(1L, 4L, 2L), ids(tree.getRoots()));
        assertNull(tree.getNode(2L).getPid());
        assertNull(tree.getNode(2L).getParent());
        assertEquals(Arrays.asList(2L, 3L), ids(tree.getPath(3L)));
        tree.moveNode(4L, 2L);
        assertEquals(Arrays.asList(1L, 2L), ids(tree.getRoots()));
        assertEquals(Arrays.asList(3L, 4L), ids(tree.getNode(2L).getChildren()));
    }

    @Test
    public void invalidMovesDoNotChangeTheTree() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        List<Long> before = ids(tree.toFlat());
        invalid(() -> tree.moveNode(999L, 1L));
        invalid(() -> tree.moveNode(2L, 999L));
        invalid(() -> tree.moveNode(2L, 2L));
        invalid(() -> tree.moveNode(1L, 3L));
        assertEquals(before, ids(tree.toFlat()));
        assertSame(tree.getNode(1L), tree.getNode(2L).getParent());
        assertSame(tree.getNode(2L), tree.getNode(3L).getParent());
    }

    @Test
    public void movingToTheSameParentKeepsSiblingAndRootOrder() {
        Tree<Node<Long>, Long> tree = new Tree<>(Arrays.asList(new Node<>(1L, null, "root"),
                new Node<>(2L, 1L, "a"), new Node<>(3L, 1L, "b"), new Node<>(4L, null, "other")));
        tree.moveNode(2L, 1L);
        tree.moveNode(1L, null);
        assertEquals(Arrays.asList(2L, 3L), ids(tree.getNode(1L).getChildren()));
        assertEquals(Arrays.asList(1L, 4L), ids(tree.getRoots()));
    }

    @Test
    public void movingAnOrphanMakesItReachableWithoutChangingItsId() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        tree.moveNode(5L, 2L);
        assertEquals(Arrays.asList(1L, 2L, 5L), ids(tree.getPath(5L)));
        assertEquals(Arrays.asList(1L, 2L, 3L, 5L, 4L), ids(tree.toFlat()));
        assertEquals(5, tree.getSize());
    }

    @Test
    public void removingAnOrphansChildDetachesItFromTheIndexedParent() {
        Tree<Node<Long>, Long> tree = new Tree<>(Arrays.asList(new Node<>(1L, null, "root"),
                new Node<>(5L, 99L, "orphan"), new Node<>(6L, 5L, "orphan-child")));
        tree.removeNode(6L);
        assertFalse(tree.hasChildren(5L));
        assertNull(tree.getNode(6L));
        tree.moveNode(5L, 1L);
        assertEquals(Arrays.asList(1L, 5L), ids(tree.toFlat()));
    }

    @Test
    public void movingAndRemovingUsesIdentityWhenNodesHaveCustomEquality() {
        Tree<Node<Long>, Long> tree = new Tree<>(Arrays.asList(new EqualNode(1L), new EqualNode(2L), new EqualNode(3L)));
        tree.moveNode(2L, 3L);
        assertEquals(Arrays.asList(1L, 3L), ids(tree.getRoots()));
        assertSame(tree.getNode(2L), tree.getNode(3L).getChildren().get(0));
        tree.removeNode(3L);
        assertEquals(Collections.singletonList(1L), ids(tree.getRoots()));
        assertEquals(1, tree.getSize());
    }

    @Test
    public void pathsIncludeSelfAndStartAtTheHighestKnownAncestor() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        assertEquals(Arrays.asList(1L, 2L, 3L), ids(tree.getPath(3L)));
        assertSame(tree.getNode(1L), tree.getPath(3L).get(0));
        assertEquals(Collections.singletonList(1L), ids(tree.getPath(1L)));
        assertEquals(Collections.singletonList(5L), ids(tree.getPath(5L)));
        assertTrue(tree.getPath(999L).isEmpty());
    }

    @Test
    public void filteringKeepsMatchesAndAncestorsAsIndependentNodeCopies() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        Tree<Node<Long>, Long> filtered = tree.filterTree(node -> node.getId() == 3L);
        assertEquals(Arrays.asList(1L, 2L, 3L), ids(filtered.toFlat()));
        assertEquals(3, filtered.getSize());
        assertNotSame(tree.getNode(1L), filtered.getNode(1L));
        assertSame(filtered.getNode(1L), filtered.getNode(2L).getParent());
        filtered.updateNode(2L, Collections.singletonMap("label", "filtered-only"));
        filtered.removeNode(3L);
        assertEquals("branch", tree.getNode(2L).label);
        assertTrue(tree.hasChildren(2L));
        assertSame(tree.getNode(1L).payload, filtered.getNode(1L).payload);
    }

    @Test
    public void matchingAParentDoesNotAutomaticallyRetainItsChildren() {
        Tree<Node<Long>, Long> tree = new Tree<>(fixture());
        Tree<Node<Long>, Long> filtered = tree.filterTree(node -> node.getId() == 1L);
        assertEquals(Collections.singletonList(1L), ids(filtered.toFlat()));
        assertFalse(filtered.hasChildren(1L));
        assertEquals(1, filtered.getSize());
    }

    @Test
    public void filteringPreservesCurrentSiblingOrderAfterMoves() {
        Tree<Node<Long>, Long> tree = new Tree<>(Arrays.asList(new Node<>(1L, null, "root"),
                new Node<>(2L, 1L, "a"), new Node<>(3L, 1L, "b")));
        tree.moveNode(2L, null);
        tree.moveNode(2L, 1L);
        Tree<Node<Long>, Long> filtered = tree.filterTree(node -> true);
        assertEquals(Arrays.asList(3L, 2L), ids(filtered.getNode(1L).getChildren()));
        assertEquals(ids(tree.toFlat()), ids(filtered.toFlat()));
    }

    @Test
    public void filteringHandlesEmptyMatchesAndOrphanBranches() {
        Tree<Node<Long>, Long> tree = new Tree<>(Arrays.asList(new Node<>(1L, null, "root"),
                new Node<>(5L, 99L, "orphan"), new Node<>(6L, 5L, "orphan-child")));
        Tree<Node<Long>, Long> empty = tree.filterTree(node -> false);
        assertEquals(0, empty.getSize());
        assertTrue(empty.isEmpty());
        Tree<Node<Long>, Long> filtered = tree.filterTree(node -> node.getId() == 6L);
        assertEquals(2, filtered.getSize());
        assertTrue(filtered.isEmpty());
        assertEquals(Arrays.asList(5L, 6L), ids(filtered.getPath(6L)));
        assertSame(filtered.getNode(5L), filtered.getNode(6L).getParent());
        assertEquals(Long.valueOf(99), filtered.getNode(5L).getPid());
    }

    @Test
    public void filteringReusesTheConfiguredCopierForNodesWithoutANoArgConstructor() {
        NoDefaultNode root = new NoDefaultNode("root");
        NoDefaultNode child = new NoDefaultNode("child");
        child.setPid("root");
        Tree<NoDefaultNode, String> tree = new Tree<>(Arrays.asList(root, child), NoDefaultNode::new);
        Tree<NoDefaultNode, String> filtered = tree.filterTree(node -> node.getId().equals("child"));
        assertEquals(2, filtered.getSize());
        assertNotSame(tree.getNode("root"), filtered.getNode("root"));
        assertSame(filtered.getNode("root"), filtered.getNode("child").getParent());
    }

    @Test
    public void updatesCannotChangeAnIdThroughADifferentlyNamedBackingField() {
        Tree<Node<Long>, Long> tree = new Tree<>(Collections.singletonList(new AliasNode(1L)));
        invalid(() -> tree.updateNode(1L, Collections.singletonMap("key", 99L)));
        assertEquals(Long.valueOf(1), tree.getNode(1L).getId());
        assertNull(tree.getNode(99L));
    }

    @Test
    public void deepSubtreesSupportAdditionTraversalFilteringAndRemovalWithoutRecursion() {
        int size = 12000;
        Node<Long> root = new Node<>(0L, null, "root");
        Node<Long> current = root;
        for (long id = 1; id < size; id++) {
            Node<Long> child = new Node<>(id, null, "child");
            current.setChildren(Collections.singletonList(child));
            current = child;
        }
        Tree<Node<Long>, Long> tree = new Tree<>(Collections.<Node<Long>>emptyList());
        tree.addNode(null, root);
        assertEquals(size, tree.getSize());
        assertEquals(size, tree.getPath((long) size - 1).size());
        assertEquals(size, tree.toFlat().size());
        Tree<Node<Long>, Long> filtered = tree.filterTree(node -> node.getId() == size - 1);
        assertEquals(size, filtered.getSize());
        tree.removeNode(0L);
        assertEquals(0, tree.getSize());
        assertEquals(size, filtered.toFlat().size());
    }

    private void invalid(Runnable action) {
        try {
            action.run();
            fail("Expected invalid tree operation");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    static class AliasNode extends Node<Long> {
        private Long key;
        private AliasNode() { super(null, null, "alias"); }
        AliasNode(Long id) { super(id, null, "alias"); key = id; }
        @Override public Long getId() { return key; }
        @Override public void setId(Long id) { key = id; }
    }
}
