import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Node {
    int key;
    boolean state; // true means it has data
    Node left, right, parent;

    private final AtomicBoolean deleted = new AtomicBoolean();
    private final ReentrantReadWriteLock leftLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock rightLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();

    public Node(int key) {
        this.key = key;
    }

    void tryLockLeftEdgeRef(Node exp, ThreadLocal<Stack<Lock>> localLocks) {
        // the child node has not changed, and the current node is not deleted
        ReentrantReadWriteLock.WriteLock lock = leftLock.writeLock();
        lock.lock();
        localLocks.get().push(lock);
        if (deleted.get() || left != exp) {
            throw new RuntimeException();
        }
    }

    void tryLockRightEdgeRef(Node exp, ThreadLocal<Stack<Lock>> localLocks) {
        // the child node has not changed, and the current node is not deleted
        ReentrantReadWriteLock.WriteLock lock = rightLock.writeLock();
        lock.lock();
        localLocks.get().push(lock);
        if (deleted.get() || right != exp) {
            throw new RuntimeException();
        }
    }

    void tryLockLeftEdgeVal(int exp, ThreadLocal<Stack<Lock>> localLocks) {
        //the node could have changed by the value inside does not, and the current node is not deleted
        ReentrantReadWriteLock.WriteLock lock = leftLock.writeLock();
        lock.lock();
        localLocks.get().push(lock);
        if (deleted.get() || Objects.isNull(left) || left.getKey() != exp) {
            throw new RuntimeException();
        }
    }

    void tryLockRightEdgeVal(int exp, ThreadLocal<Stack<Lock>> localLocks) {
        //the node could have changed by the value inside does not, and the current node is not deleted
        ReentrantReadWriteLock.WriteLock lock = rightLock.writeLock();
        lock.lock();
        localLocks.get().push(lock);
        if (deleted.get() || Objects.isNull(right) || right.getKey() != exp) {
            throw new RuntimeException();
        }
    }

    void tryReadLockState(ThreadLocal<Stack<Lock>> localLocks) {
        ReentrantReadWriteLock.ReadLock lock = stateLock.readLock();
        lock.lock();
        if (!deleted.get())
            localLocks.get().push(lock);
        else
            lock.unlock();
    }

    void tryReadLockState(boolean exp, ThreadLocal<Stack<Lock>> localLocks) {
        //value of the state is equal to exp and the current node is not deleted
        ReentrantReadWriteLock.ReadLock lock = stateLock.readLock();
        lock.lock();
        if (exp == state && !deleted.get())
            localLocks.get().push(lock);
        else
            lock.unlock();
    }

    void tryWriteLockState(boolean exp, ThreadLocal<Stack<Lock>> localLocks) {
        //value of the state is equal to exp and the current node is not deleted
        ReentrantReadWriteLock.WriteLock lock = stateLock.writeLock();
        lock.lock();
        if (exp == state && !deleted.get())
            localLocks.get().push(lock);
        else
            lock.unlock();
    }

    void tryLockEdgeRef(Node exp, ThreadLocal<Stack<Lock>> localLocks) {
        //locks left or right depending on the key
        if (key < exp.getKey()) {
            tryLockRightEdgeRef(exp, localLocks);
        } else {
            tryLockLeftEdgeRef(exp, localLocks);
        }
    }

    void tryLockEdgeVal(Node exp, ThreadLocal<Stack<Lock>> localLocks) {
        //locks left or right depending on the key
        if (key < exp.getKey()) {
            tryLockRightEdgeVal(exp.getKey(), localLocks);
        } else {
            tryLockLeftEdgeVal(exp.getKey(), localLocks);
        }
    }

    public int getKey() {
        return key;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node node) {
        left = node;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node node) {
        right = node;
    }

    public AtomicBoolean deleted() {
        return deleted;
    }
}
