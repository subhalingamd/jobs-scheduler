package PriorityQueue;

import java.util.ArrayList;

public class MaxHeap<T extends Comparable> implements PriorityQueueInterface<T> {

	ArrayList<Wrap<T>> arr;
	int ct; 
	int token;

    public MaxHeap(){
        arr=new ArrayList();
        ct=0; 
        token=0;
    }

    public MaxHeap (ArrayList<Wrap<T>> arr){
        this.arr=arr;
        ct=arr.size();
        token=arr.size();
    }

    @Override
    public void insert(T element) {
    	arr.add(new Wrap<T>(element,token++));
    	int i=ct++;
    	Wrap<T> app=arr.get(i);
    	while (i>0 && app.compareTo(arr.get((int)((i-1)/2)))>0){
    		arr.set(i,arr.get((int)(i-1)/2));
    		i=(int)(i-1)/2;
    	}
    	arr.set(i,app);
    }

    @Override
    public T extractMax() {
    	if (arr.isEmpty())
        	return null;

        ct--;
        T ret=arr.get(0).el;
        Wrap<T> rem=arr.get(ct);
        //arr.set(0,rem);
        arr.remove(ct);

        if (ct==0)
        	return ret;
        
        int i=0;
        
    	while (2*i+1<ct){
    		int probed;
    		if (2*i+2>=arr.size()){ //ONE CHILD
                
                if (rem.compareTo(arr.get(2*i+1))<0){
                    arr.set(i,arr.get(2*i+1));
                    probed=2*i+1;
                }
                else
                    break;
    		}
            else{
                Wrap<T> maxChild;
                if (arr.get(2*i+1).compareTo(arr.get(2*i+2))>0){
                    maxChild=arr.get(2*i+1);
                    probed=2*i+1;
                }
                else{
                    maxChild=arr.get(2*i+2);
                    probed=2*i+2;
                                                                    // Not equal, get Max
                }
                if (rem.compareTo(maxChild)<0){
                    arr.set(i,maxChild);
                }
                else
                    break;
            }

    		i=probed;
    	}

    	arr.set(i,rem);


    	return ret;     
    }

    public boolean isEmpty(){
        return arr.isEmpty();
    }

}