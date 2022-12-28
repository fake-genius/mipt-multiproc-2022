import java.util.*;
import java.util.concurrent.locks.Lock;

public class BST {

    final boolean STATE_DATA = true;
    final boolean STATE_ROUTING = false;
    Node root;
    static ThreadLocal<Stack<Lock>> localLocks = ThreadLocal.withInitial(Stack::new);

    public BST(int key) {
        root = new Node(key);
    }

    boolean isDeleted(Node node) {
        return Objects.nonNull(node) && node.deleted().get();
    }

    public List<Node> traversal(int key) {
        while (true) {
            Node gprev = new Node(-1);
            Node prev = new Node(-1);
            Node curr = root;

            while (Objects.nonNull(curr)) {
                if (curr.getKey() == key)
                    break;

                gprev = prev;
                prev = curr;
                if (curr.getKey() > key)
                    curr = curr.getLeft();
                else
                    curr = curr.getRight();

                if (isDeleted(gprev))
                    break;
                if (isDeleted(prev))
                    break;
                if (isDeleted(curr))
                    break;
            }

            if (isDeleted(gprev))
                continue;
            if (isDeleted(prev))
                continue;
            if (isDeleted(curr))
                continue;

            return Arrays.asList(gprev, prev, curr);
        }
    }

    public boolean contains(int key) {
        //System.out.println("Searching " + key);
        List<Node> traversal = traversal(key);
        Node curr = traversal.get(2);
        return Objects.nonNull(curr) && curr.getState() == STATE_DATA;
    }

    public boolean insert(int key) {
        //System.out.println("Inserting " + key);
        while (true) {
            try {
                List<Node> traversal = traversal(key);
                Node prev = traversal.get(1);
                Node curr = traversal.get(2);

                if (Objects.nonNull(curr)) {
                    if (curr.getState() == STATE_DATA) {
                        return false;
                    }
                    curr.tryWriteLockState(STATE_ROUTING, localLocks);
                    curr.setState(STATE_DATA);
                } else { // node has value v or is a place to insert
                    Node newNode = new Node(key);
                    newNode.setState(STATE_DATA);
                    if (prev.getKey() > key) {
                        prev.tryReadLockState(localLocks);
                        prev.tryLockLeftEdgeRef(null, localLocks);
                        prev.setLeft(newNode);
                    } else {
                        prev.tryReadLockState(localLocks);
                        prev.tryLockRightEdgeRef(null, localLocks);
                        prev.setRight(newNode);
                    }
                }
                return true;
            } catch (Exception e) {
                //System.out.println("Catch in insert: " +e.toString());
            } finally {
                unlockAll();
            }
        }
    }

    public boolean delete(int key) {
        //System.out.println("Deleting " + key);
        while (true) {
            try {
                List<Node> traversal = traversal(key);
                Node gprev = traversal.get(0);
                Node prev = traversal.get(1);
                Node curr = traversal.get(2);

                if (Objects.isNull(curr) || curr.getState() != STATE_DATA) {
                    return false;
                }

                if (Objects.nonNull(curr.getLeft()) && Objects.nonNull(curr.getRight())) { // 2 children
                    //lockVertexWithTwoChildren(curr);
                    curr.tryWriteLockState(STATE_DATA, localLocks);

                    if (Objects.isNull(curr.getLeft()) || Objects.isNull(curr.getRight())) {
                        throw new RuntimeException("in delete in 2 children");
                    }
                    curr.setState(STATE_ROUTING);
                } else if (Objects.nonNull(curr.getLeft()) || Objects.nonNull(curr.getRight())) { // 1 child
                    Node child = Objects.nonNull(curr.getLeft()) ? curr.getLeft() : curr.getRight();

                    if (curr.getKey() < prev.getKey()) {
                        lockVertexWithOneChild(prev, curr, child);
                        curr.deleted().set(true);
                        prev.setLeft(child);
                    } else {
                        lockVertexWithOneChild(prev, curr, child);
                        curr.deleted().set(true);
                        prev.setRight(child);
                    }
                } else { //has data
                    if (prev.getState() == STATE_DATA) {
                        if (curr.getKey() < prev.getKey()) { //is left child
                            prev.tryReadLockState(STATE_DATA, localLocks);
                            curr = lockLeaf(key, prev, curr);
                            curr.deleted().set(true);
                            prev.setLeft(null);
                        } else { //is right child
                            prev.tryReadLockState(STATE_DATA, localLocks);
                            curr = lockLeaf(key, prev, curr);
                            curr.deleted().set(true);
                            prev.setRight(null);
                        }
                    } else { //is routing
                        Node child = curr.getKey() < prev.getKey() ? prev.getRight() : prev.getLeft();

                        if (curr.getKey() < prev.getKey()) {
                            child = prev.getRight();
                        } else {
                            child = prev.getLeft();
                        }


                        if (Objects.nonNull(gprev.getLeft()) && prev == gprev.getLeft()) { //prev is left child
                            gprev.tryLockEdgeRef(prev, localLocks);
                            prev.tryWriteLockState(STATE_ROUTING, localLocks);
                            prev.tryLockEdgeRef(child, localLocks);

                            curr = lockLeaf(key, prev, curr);

                            prev.deleted().set(true);
                            curr.deleted().set(true);
                            gprev.setLeft(child);
                        } else if (Objects.nonNull(gprev.getRight()) && prev == gprev.getRight()) { //prev is right child
                            gprev.tryLockEdgeRef(prev, localLocks);
                            prev.tryWriteLockState(STATE_ROUTING, localLocks);
                            prev.tryLockEdgeRef(child, localLocks);

                            curr = lockLeaf(key, prev, curr);

                            prev.deleted().set(true);
                            curr.deleted().set(true);
                            gprev.setRight(child);
                        }

                    }
                }

                return true;
            } catch (Exception e) {
                //System.out.println("Catch in delete: " + e.toString());
            } finally {
                unlockAll();
            }
        }
    }

    void unlockAll() {
        while (!localLocks.get().empty()) {
            localLocks.get().pop().unlock();
        }
    }

    Node lockLeaf(int key, Node prev, Node curr) {
        prev.tryLockEdgeVal(curr, localLocks);
        //curr = key < prev.getKey() ? prev.getLeft() : prev.getRight();

        if (key < prev.getKey())
            curr = prev.getLeft();
        else
            curr = prev.getRight();


        try {
            curr.tryWriteLockState(STATE_DATA, localLocks);
        } catch (Exception e) {
            System.out.println("Catch in lockLeaf: " + e.toString());
        }

        if (Objects.nonNull(curr.getLeft()) || Objects.nonNull(curr.getRight())) {
            throw new RuntimeException("Tried to lock a leaf, but node is not a leaf");
        }
        return curr;
    }

    void lockVertexWithOneChild(Node prev, Node curr, Node child) {
        prev.tryLockEdgeRef(curr, localLocks);
        try {
            curr.tryWriteLockState(STATE_DATA, localLocks);
        } catch (Exception e) {
            System.out.println("Catch in lockVertexWithOneChild: " + e.toString());
        }

        if (Objects.nonNull(curr.getLeft()) && Objects.nonNull(curr.getRight())) {
            throw new RuntimeException("Tried to lock node and it's only child, but node has two of them");
        }

        if (Objects.isNull(curr.getLeft()) && Objects.isNull(curr.getRight())) {
            throw new RuntimeException("Tried to lock node and it's only child, but node has none of them");
        }

        curr.tryLockEdgeRef(child, localLocks);
    }

    void lockVertexWithTwoChildren(Node curr) {
        try {
            curr.tryWriteLockState(STATE_DATA, localLocks);
        } catch (Exception e) {
            System.out.println("Catch in lockVertexWithTwoChildren: " + e.toString());
        }

        if (Objects.isNull(curr.getLeft()) || Objects.isNull(curr.getRight())) {
            throw new RuntimeException("Tried to lock node and it's children but it has less than two of them");
        }
    }

    public List<Integer> getTraversal() {
        List<Integer> keys = new ArrayList<>();
        Stack<Node> stack = new Stack<>();
        Node curr = root;
        while (curr != null || !stack.empty()) {
            while (curr != null) {
                stack.push(curr);
                curr = curr.getLeft();
            }
            curr = stack.pop();
            if (curr.getState() == STATE_DATA && !curr.deleted().get()) {
                keys.add(curr.getKey());
            }
            curr = curr.getRight();
        }
        return keys;
    }

    boolean checkKeys(Node curr) {
        boolean flag = true;
        if (Objects.nonNull(curr.getRight())) {
            if (curr.getKey() > curr.getRight().getKey())
                return false;
            flag = checkKeys(curr.getRight());
        }
        if (Objects.nonNull(curr.getLeft())) {
            if (curr.getKey() < curr.getLeft().getKey())
                return false;
            flag &= checkKeys(curr.getLeft());
        }
        return flag;
    }

    public void checkTree() {
        if (!checkKeys(root))
            System.out.println("------Bad ordered keys found------");
        else
            System.out.println("Parallel keys correct!");
    }
}
