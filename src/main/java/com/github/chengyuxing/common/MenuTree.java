package com.github.chengyuxing.common;

import java.util.*;

/**
 * 菜单树生成帮助类
 */
public class MenuTree {
    /**
     * 树节点配置类
     */
    public static abstract class Tree extends HashMap<Object, Object> {
        /**
         * 指定数据节点中id键名
         *
         * @return id键名
         */
        protected abstract String idKey();

        /**
         * 指定数据节点中父id键名
         *
         * @return 父id键名
         */
        protected abstract String pidKey();

        /**
         * 数据节点中的子节点集合键名，默认为 'children'
         *
         * @return 子节点集合键名
         */
        public String childrenKey() {
            return "children";
        }

        /**
         * 构造函数
         *
         * @param node 节点
         */
        public Tree(Map<?, ?> node) {
            super(node);
            put(childrenKey(), new ArrayList<>());
        }

        /**
         * 获取id
         *
         * @return id
         */
        public Object getId() {
            return get(idKey());
        }

        /**
         * 获取父id
         *
         * @return 父id
         */
        public Object getPid() {
            return get(pidKey());
        }

        /**
         * 获取子节点集合
         *
         * @return 子节点集合
         */
        @SuppressWarnings("unchecked")
        public List<Tree> getChildren() {
            return (List<Tree>) get(childrenKey());
        }
    }

    private final List<Tree> nodes;

    /**
     * 构造函数
     *
     * @param nodes 节点数据
     */
    public MenuTree(List<Tree> nodes) {
        this.nodes = new ArrayList<>(nodes);
    }

    /**
     * 创建一个树结构数据
     *
     * @param root 根结点
     * @return 树结构数据
     */
    public Tree create(Tree root) {
        List<Tree> rootNode = new ArrayList<>();
        rootNode.add(root);
        List<Tree> newNodes = new ArrayList<>(nodes);
        nodesAgg(newNodes, rootNode);
        return rootNode.get(0);
    }

    /**
     * 节点聚合操作
     *
     * @param treeNodes 节点数据
     * @param root      根结点
     */
    private static void nodesAgg(List<Tree> treeNodes, List<Tree> root) {
        if (treeNodes.isEmpty()) {
            return;
        }
        for (int i = root.size() - 1, j = 0; i >= j; i--, j++) {
            Iterator<Tree> iterator = treeNodes.iterator();
            while (iterator.hasNext()) {
                Tree next = iterator.next();
                Tree backward = root.get(i);
                Tree forward = root.get(j);
                if (forward.getId().equals(next.getPid())) {
                    forward.getChildren().add(next);
                    iterator.remove();
                } else if (backward.getId().equals(next.getPid())) {
                    backward.getChildren().add(next);
                    iterator.remove();
                }
            }
            nodesAgg(treeNodes, root.get(j).getChildren());
        }
    }
}
