package com.github.chengyuxing.common.tree;

import java.util.List;

/**
 * Mutable node contract for {@link Tree}. A null parent ID denotes a root.
 *
 * @param <T> concrete node type
 * @param <ID> node identifier type
 */
public interface TreeNode<T, ID> {
    ID getId();

    void setId(ID id);

    ID getPid();

    void setPid(ID pid);

    List<T> getChildren();

    void setChildren(List<T> children);

    T getParent();

    void setParent(T parent);
}
