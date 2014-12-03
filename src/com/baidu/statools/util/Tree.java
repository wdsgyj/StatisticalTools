package com.baidu.statools.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created on 2014/11/14.
 *
 * @author clark
 */
public class Tree<T> {
    Tree<T> parent;
    Map<T, Tree<T>> children = new LinkedHashMap<T, Tree<T>>();
    T value;

    Map<T, Tree<T>> allNodes = new HashMap<T, Tree<T>>();

    private Tree(Tree<T> parent, T value) {
        this.parent = parent;
        this.value = value;
        allNodes.put(value, this);
    }

    public Tree(T value) {
        this(null, value);
    }

    //--------------------------------------------------
    public Tree<T> getParent() {
        return parent;
    }

    public List<Tree<T>> getParents() {
        LinkedList<Tree<T>> trees = new LinkedList<Tree<T>>();
        for (Tree<T> node = this.parent; node != null; node = node.parent) {
            trees.add(node);
        }
        return trees;
    }

    public Set<Tree<T>> getDirectChildren() {
        return new HashSet<Tree<T>>(children.values());
    }

    public Set<Tree<T>> getAllChildren() {
        Collection<Tree<T>> trees = allNodes.values();
        HashSet<Tree<T>> set = new HashSet<Tree<T>>(trees);
        // 把自身移除
        set.remove(this);
        return set;
    }

    public Set<T> getDirectChildrenValue() {
        return new HashSet<T>(children.keySet());
    }

    public Set<T> getAllChildrenValue() {
        return new HashSet<T>(allNodes.keySet());
    }

    public int getNodeCount() {
        return allNodes.size();
    }

    public int getDirectChildrenCount() {
        return children.size();
    }

    public T getValue() {
        return value;
    }

    //--------------------------------------------------
    public Tree<T> getRandomAccessNode(T t) {
        return allNodes.get(t);
    }

    public Tree<T> removeRandomAccessNode(T t) {
        Tree<T> rs = allNodes.get(t);
        // 不能移除不存在的 node 和自身
        if (rs != null && rs.parent != null) {
            rs.parent.removeChild(t);
        }
        return rs;
    }

    //--------------------------------------------------
    public Tree<T> addChild(T value) {
        if (allNodes.containsKey(value)) {
            return null;
        }

        Tree<T> newTree = new Tree<T>(this, value);
        children.put(value, newTree);
        allNodes.put(value, newTree);

        final List<Tree<T>> parents = getParents();
        for (Tree<T> parent : parents) {
            parent.allNodes.put(value, newTree);
        }

        return newTree;
    }

    public boolean addChild(Tree<T> tree) {
        if (allNodes.containsKey(tree.value)) {
            return false;
        }

        for (Map.Entry<T, Tree<T>> entry : tree.allNodes.entrySet()) {
            final T childValue = entry.getKey();
            final Tree<T> childTree = entry.getValue();
            final List<Tree<T>> parents = getParents();

            // 添加到自身的随机访问 table 中
            allNodes.put(childValue, childTree);

            // 轮训添加到 parent 的随机访问 table 中
            for (Tree<T> parent : parents) {
                parent.allNodes.put(childValue, childTree);
            }
        }

        // 添加到直接 children 中
        children.put(tree.value, tree);
        tree.parent = this;

        return true;
    }

    public Tree<T> getChild(T t) {
        return children.get(t);
    }

    public Tree<T> removeChild(T t) {
        Tree<T> rs = children.remove(t);
        if (rs != null) {
            final List<Tree<T>> parents = getParents();

            // 在自身和 parent 的随机访问 table 中删除
            for (T childTree : rs.depthFirst()) {
                allNodes.remove(childTree);

                for (Tree<T> parent : parents) {
                    parent.allNodes.remove(childTree);
                }
            }

            rs.parent = null;
        }
        return rs;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public String toString() {
        return depthFirstToString();
    }

    public String depthFirstToString() {
        StringBuilder builder = new StringBuilder("Tree { ");
        for (T t : depthFirst()) {
            builder.append(t).append(" ");
        }
        builder.append("}");
        return builder.toString();
    }

    public String breathFirstToString() {
        StringBuilder builder = new StringBuilder("Tree { ");
        for (T t : breathFirst()) {
            builder.append(t).append(" ");
        }
        builder.append("}");
        return builder.toString();
    }

    public Iterable<T> depthFirst() {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new DepthFirstIterator<T>(Tree.this);
            }
        };
    }

    public Iterable<T> breathFirst() {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new BreathFirstIterator<T>(Tree.this);
            }
        };
    }

    private static class DepthFirstIterator<T> implements Iterator<T> {
        private LinkedList<Tree<T>> nodes = new LinkedList<Tree<T>>();

        DepthFirstIterator(Tree<T> node) {
            nodes.add(node);
        }

        @Override
        public boolean hasNext() {
            return nodes.size() > 0;
        }

        @Override
        public T next() {
            Tree<T> node = nodes.removeLast();
            LinkedList<Tree<T>> stack = new LinkedList<Tree<T>>();

            // 逆序添加 child
            for (Tree<T> n : node.children.values()) {
                stack.addLast(n);
            }
            while (stack.size() > 0) {
                nodes.addLast(stack.removeLast());
            }

            return node.value;
        }

        @Override
        public void remove() {
        }
    }

    private static class BreathFirstIterator<T> implements Iterator<T> {
        private LinkedList<Tree<T>> nodes = new LinkedList<Tree<T>>();

        BreathFirstIterator(Tree<T> node) {
            nodes.add(node);
        }

        @Override
        public boolean hasNext() {
            return nodes.size() > 0;
        }

        @Override
        public T next() {
            Tree<T> node = nodes.removeFirst();

            // 顺序添加 child
            for (Tree<T> n : node.children.values()) {
                nodes.addLast(n);
            }

            return node.value;
        }

        @Override
        public void remove() {
        }
    }
}
