package ProjectManagement;

import java.util.ArrayList;


public class Project implements Comparable<Project>{
	String name;
	int priority,budget;
    ArrayList<Job>[] jobs; 
    int id;


	public Project(String name,int priority,int budget, int id){
		this.name=name;
		this.budget=budget;
		this.priority=priority;
		jobs=new ArrayList[2];
        jobs[0]=new ArrayList<Job>();
        jobs[1]=new ArrayList<Job>();
        id=this.id;
	}

	public void addBudget(int append){
		budget+=append;
	}

	public int getBudget(){
		return budget;
	}

	public int getPriority(){
		return priority;
	}

	public void addJob(Job job){
        jobs[0].add(job);
    }

    public void completeJob(Job job){
        jobs[1].add(job);
    }

    public ArrayList<Job>[] getJobs(){
        return jobs;
    }

	@Override
	public String toString(){
		return name;
	}

	@Override
	public int compareTo(Project p){
		if (this.priority>p.priority)
			return 1;
		else if (this.priority==p.priority){
			if (this.id<p.id)
				return 1;
			else
				return -1;
		}
		else
			return -1;
	}
}
