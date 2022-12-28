import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class BSTNaive {
    //node class that defines BST node
    class Node {
        int value;
        Node left;
        Node right;

        Node(int value) {
            this.value = value;
            right = null;
            left = null;
        }
    }
    // BST root node
    Node root;

    // Constructor for BST => initial empty tree
    public BSTNaive(){
        root = null;
    }
    //delete a node from BST
    public void delete(int value) {
        root = deleteRecursive(root, value);
    }

    //recursive delete function
    private Node deleteRecursive(Node current, int value) {
        if (current == null) {
            return null;
        }

        if (value == current.value) {
            if (current.left == null && current.right == null) {
                return null;
            }
            if (current.right == null) {
                return current.left;
            }

            if (current.left == null) {
                return current.right;
            }

            int smallestValue = findSmallestValue(current.right);
            current.value = smallestValue;
            current.right = deleteRecursive(current.right, smallestValue);
            return current;
        }
        if (value < current.value) {
            current.left = deleteRecursive(current.left, value);
            return current;
        }
        current.right = deleteRecursive(current.right, value);
        return current;
    }

    private int findSmallestValue(Node root) {
        return root.left == null ? root.value : findSmallestValue(root.left);
    }

    // insert a node in BST
    private Node addRecursive(Node current, int value) {
        if (current == null) {
            return new Node(value);
        }

        if (value < current.value) {
            current.left = addRecursive(current.left, value);
        } else if (value > current.value) {
            current.right = addRecursive(current.right, value);
        } else {
            // value already exists
            return current;
        }

        return current;
    }

    public void insert(int value) {
        root = addRecursive(root, value);
    }

    // method for inorder traversal of BST
    void traverse() {
        traverseInOrder(root);
        System.out.println();
    }

    // recursively traverse the BST
    public void traverseInOrder(Node node) {
        if (node != null) {
            traverseInOrder(node.left);
            System.out.print(" " + node.value);
            traverseInOrder(node.right);
        }
    }

    public boolean contains(int value) {
        return containsNodeRecursive(root, value);
    }

    //recursive insert function
    private boolean containsNodeRecursive(Node current, int value) {
        if (current == null) {
            return false;
        }
        if (value == current.value) {
            return true;
        }
        return value < current.value
                ? containsNodeRecursive(current.left, value)
                : containsNodeRecursive(current.right, value);
    }

    public List<Integer> getTraversal() {
        //traverse();
        List<Integer> keys = new ArrayList<>();
        Stack<Node> stack = new Stack<>();
        Node curr = root;
        while (curr != null || !stack.empty()) {
            while (curr != null) {
                stack.push(curr);
                curr = curr.left;
            }
            curr = stack.pop();
            //if (curr.getState() == STATE_DATA ) {
            keys.add(curr.value);
            //}
            curr = curr.right;
        }
        return keys;
    }

    boolean checkKeys(Node curr) {
        boolean flag = true;
        if (curr.right != null) {
            if (curr.value > curr.right.value)
                return false;
            flag = checkKeys(curr.right);
        }
        if (Objects.nonNull(curr.left)) {
            if (curr.value < curr.left.value)
                return false;
            flag &= checkKeys(curr.left);
        }
        return flag;
    }

    public void checkTree() {
        if (!checkKeys(root))
            System.out.println("------Bad ordered keys found------");
        else
            System.out.println("Sequential keys correct!");
    }
}
