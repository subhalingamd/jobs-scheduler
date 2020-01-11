package PriorityQueue;

	public class Wrap<T extends Comparable> implements Comparable<Wrap<T>>{
		T el;
		int stamp;

		public Wrap(T el,int stamp){
			this.el=el;
			this.stamp=stamp;
		}

        @Override
        public int compareTo(Wrap<T> W){
            if (this.el.compareTo(W.el)!=0)
                return this.el.compareTo(W.el);
            if (this.stamp<W.stamp)
                return 1;
            else
                return -1;
        }
	}