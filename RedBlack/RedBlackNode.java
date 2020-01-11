package RedBlack;

import Util.RBNodeInterface;

import java.util.List;
import java.util.ArrayList;

public class RedBlackNode<T extends Comparable, E> implements RBNodeInterface<E> {
	T key;
	List<E> data=new ArrayList<E>();
	RedBlackNode<T,E> left,right,parent;
	char color;

	public RedBlackNode(T key,E value,RedBlackNode<T,E> parent){
		this.key=key;
		this.data.add(value);
		this.parent=parent;
		this.color='R';
		left=null;
		right=null;
	}

	public void addData(E data){
		this.data.add(data);
	}

	public void setColor(char color){
		this.color=color;
	}

	public void changeParent(RedBlackNode<T,E> parent){
		this.parent=parent;
	}

	public void makeLeftChild(RedBlackNode<T,E> child){
		this.left=child;
	}

	public void makeRightChild(RedBlackNode<T,E> child){
		this.right=child;
	}

	public T getKey(){
		return key;
	}

	public RedBlackNode<T,E> getParent(){
		return parent;
	}

	public RedBlackNode<T,E> getLeftChild(){
		return left;
	}

	public RedBlackNode<T,E> getRightChild(){
		return right;
	}

	public char getColor(){
		return color;
	}

	public void clearValue(){
		data=new ArrayList<E>();
	}

    @Override
    public E getValue() {
    	if (data.size()<=0)
    		return null;
        return data.get(0);
    }

    @Override
    public List<E> getValues() {
    	if (data.size()<=0)
    		return null;
    	if (data.get(0)==null)
    		return null;
        return data;
    }
}
