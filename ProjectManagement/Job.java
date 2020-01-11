package ProjectManagement;

public class Job implements Comparable<Job> {
	String name;
	User user;
	Project project;
	int runtime,id,arrtime,comptime;
	boolean completed;
	//boolean expensive; --NOT REQ
	int priority;

	public Job(String name,Project project,User user,int runtime,int id,int arrtime){
		this.name=name;
		this.user=user;
		this.project=project;
		this.runtime=runtime;
		this.id=id;
		this.arrtime=arrtime;
		this.comptime=arrtime;
		completed=false;
		//expensive=false;--NOT REQ
		this.priority=project.getPriority();
	}

	public int getRuntime(){
		return runtime;
	}

	public int getPriority(){
		return priority;
	}

	public void changePriority(int p){
		priority=p;
	}

	public Project getProject(){
		return project;
	}

	public User getUser(){
		return user;
	}

	public void setCompTime(int comptime){
		this.comptime=comptime;
	}

	public int getArrTime(){
		return arrtime;
	}

	public int getCompTime(){
		return comptime;
	}


	public void flagCompleted(){
		completed=true;
	}

	public boolean completedStatus(){
		return completed;
	}

	/* --NOT REQ
	public void flagExpensive(){
		expensive=true;
	}

	public void unflagExpensive(){
		expensive=false;
	}


	public boolean expensiveStatus(){
		return expensive;
	}
	*/


	@Override
	public String toString(){
		return name;
	}

    @Override
    public int compareTo(Job job) {
        if (this.priority<job.priority)
        	return -1;
        else if(this.priority==job.priority){
        	if (this.id<job.id)
        		return 1;
        	else
        		return -1;
        }
        else 
        	return 1;
    }
}