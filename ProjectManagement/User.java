package ProjectManagement;

import java.util.ArrayList;

public class User implements Comparable<User> {
	String name;
    int budget;
    ArrayList<Job>[] jobs; 

	public User(String name){
		this.name=name;
        jobs=new ArrayList[2];
        jobs[0]=new ArrayList<Job>();
        jobs[1]=new ArrayList<Job>();
        budget=0;
	}

    public void addJob(Job job){
        jobs[0].add(job);
    }

    public void addBudget(int b){
        budget+=b;
    }

    public int getBudget(){
        return budget;
    }

    public void completeJob(Job job){
        jobs[1].add(job);
    }

    public ArrayList<Job>[] getJobs(){
        return jobs;
    }

    @Override
    public int compareTo(User user) {
        return this.name.compareTo(user.name);
    }

    @Override
    public String toString(){
    	return name;
    }
}
