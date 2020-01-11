package ProjectManagement;


import java.io.*;
import java.net.URL;
import java.util.ArrayList;

//MY IMPORTS
import PriorityQueue.*;
import RedBlack.*;
import Trie.*;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;

public class Scheduler_Driver extends Thread implements SchedulerInterface {

    private Trie<Project> allProjects = new Trie();
    private MaxHeap<Job> allJobs = new MaxHeap();
    private Trie<Job> jobList = new Trie();
    private Trie<User> allUsers = new Trie();


    private Queue<Job> completedJobs=new LinkedList();
    //private Queue<Job> expensiveJobs=new LinkedList(); -- NOT REQ
    private RBTree<String,Job> expensiveJobsUpd=new RBTree();

    private int pendingJob=0;
    private int idGenerator=0;
    private int currTime=0;
    private int projIdGenerator=0;

    private MaxHeap<Project> projectsPriority = new MaxHeap();
    private PriorityListUsers userConsumed = new PriorityListUsers(); 
    //private PriorityListProjects projectsPriority = new PriorityListProjects(); REMOVE


    public static void main(String[] args) throws IOException {
//

        Scheduler_Driver scheduler_driver = new Scheduler_Driver();
        File file;
        if (args.length == 0) {
            URL url = Scheduler_Driver.class.getResource("INP");
            file = new File(url.getPath());
        } else {
            file = new File(args[0]);
        }

        scheduler_driver.execute(file);
    }

    public void execute(File commandFile) throws IOException {


        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(commandFile));

            String st;
            while ((st = br.readLine()) != null) {
                String[] cmd = st.split(" ");
                if (cmd.length == 0) {
                    System.err.println("Error parsing: " + st);
                    return;
                }
                String project_name, user_name;
                Integer start_time, end_time;

                long qstart_time, qend_time;

                switch (cmd[0]) {
                    case "PROJECT":
                        handle_project(cmd);
                        break;
                    case "JOB":
                        handle_job(cmd);
                        break;
                    case "USER":
                        handle_user(cmd[1]);
                        break;
                    case "QUERY":
                        handle_query(cmd[1]);
                        break;
                    case "": // HANDLE EMPTY LINE
                        handle_empty_line();
                        break;
                    case "ADD":
                        handle_add(cmd);
                        break;
                    //--------- New Queries
                    case "NEW_PROJECT":
                    case "NEW_USER":
                    case "NEW_PROJECTUSER":
                    case "NEW_PRIORITY":
                        timed_report(cmd);
                        break;
                    case "NEW_TOP":
                        qstart_time = System.nanoTime();
                        timed_top_consumer(Integer.parseInt(cmd[1]));
                        qend_time = System.nanoTime();
                        System.out.println("Time elapsed (ns): " + (qend_time - qstart_time));
                        break;
                    case "NEW_FLUSH":
                        qstart_time = System.nanoTime();
                        timed_flush( Integer.parseInt(cmd[1]));
                        qend_time = System.nanoTime();
                        System.out.println("Time elapsed (ns): " + (qend_time - qstart_time));
                        break;
                    default:
                        System.err.println("Unknown command: " + cmd[0]);
                }

            }


            run_to_completion();
            print_stats();

        } catch (FileNotFoundException e) {
            System.err.println("Input file Not found. " + commandFile.getAbsolutePath());
        } catch (NullPointerException ne) {
            ne.printStackTrace();

        }
    }

    @Override
    public ArrayList<JobReport_> timed_report(String[] cmd) {
        long qstart_time, qend_time;
        ArrayList<JobReport_> res = null;
        switch (cmd[0]) {
            case "NEW_PROJECT":
                qstart_time = System.nanoTime();
                res = handle_new_project(cmd);
                qend_time = System.nanoTime();
                System.out.println("Time elapsed (ns): " + (qend_time - qstart_time));
                break;
            case "NEW_USER":
                qstart_time = System.nanoTime();
                res = handle_new_user(cmd);
                qend_time = System.nanoTime();
                System.out.println("Time elapsed (ns): " + (qend_time - qstart_time));
                break;
            case "NEW_PROJECTUSER":
                qstart_time = System.nanoTime();
                res = handle_new_projectuser(cmd);
                qend_time = System.nanoTime();
                System.out.println("Time elapsed (ns): " + (qend_time - qstart_time));
                break;
            case "NEW_PRIORITY":
                qstart_time = System.nanoTime();
                res = handle_new_priority(cmd[1]);
                qend_time = System.nanoTime();
                System.out.println("Time elapsed (ns): " + (qend_time - qstart_time));
                break;
        }

        return res;
    }



    public void handle_project(String[] cmd) {
        System.out.println("Creating project");
        Project p=new Project(cmd[1],Integer.parseInt(cmd[2]),Integer.parseInt(cmd[3]),projIdGenerator++);
        allProjects.insert(cmd[1],p);
        projectsPriority.insert(p);
    }

    public void handle_user(String name) {
        System.out.println("Creating user");
        User u=new User(name);
        boolean f=allUsers.insert(name,u);
        if (f)
            userConsumed.add(u);

    }

    public void handle_job(String[] cmd) {
        System.out.println("Creating job");
        TrieNode<User> getUser=(TrieNode)allUsers.search(cmd[3]);
        if (getUser==null){
            System.out.println("No such user exists: "+cmd[3]);
            return;
        }
        TrieNode<Project> getProj=(TrieNode)allProjects.search(cmd[2]);
        if (getProj==null){
            System.out.println("No such project exists. "+cmd[2]);
            return;
        }
        
        User user=getUser.getValue();
        Project proj=getProj.getValue();
        Job newJob=new Job(cmd[1],proj,user,Integer.parseInt(cmd[4]),idGenerator++,currTime);
        
        //Thread t1=new Thread(()->allJobs.insert(newJob));
        //t1.start();
        //Thread t2=new Thread(()->jobList.insert(cmd[1],newJob));
        //t2.start();
        //t1.join();
        //t2.join();
        allJobs.insert(newJob);
        jobList.insert(cmd[1],newJob);
        proj.addJob(newJob);
        user.addJob(newJob);
        pendingJob++;
    }

     public void handle_query(String key) {
        System.out.println("Querying");
        System.out.print(key+": ");
        TrieNode<Job> currJobNode = (TrieNode<Job>)jobList.search(key);
        if (currJobNode==null){
            System.out.println("NO SUCH JOB");
            return;
        }
        if (currJobNode.getValue().completedStatus())
            System.out.println("COMPLETED");
        else
            System.out.println("NOT FINISHED");
    }

    public void timed_handle_project(String[] cmd) {
        Project p=new Project(cmd[1],Integer.parseInt(cmd[2]),Integer.parseInt(cmd[3]),projIdGenerator++);
        allProjects.insert(cmd[1],p);
        projectsPriority.insert(p);
    }

    public void timed_handle_user(String name) {
        User u=new User(name);
        boolean f=allUsers.insert(name,u);
        if (f)
            userConsumed.add(u);

    }

    public void timed_handle_job(String[] cmd) {
        TrieNode<User> getUser=(TrieNode)allUsers.search(cmd[3]);
        if (getUser==null){
            return;
        }
        TrieNode<Project> getProj=(TrieNode)allProjects.search(cmd[2]);
        if (getProj==null){
            return;
        }
        
        User user=getUser.getValue();
        Project proj=getProj.getValue();
        Job newJob=new Job(cmd[1],proj,user,Integer.parseInt(cmd[4]),idGenerator++,currTime);
        
        allJobs.insert(newJob);
        jobList.insert(cmd[1],newJob);
        proj.addJob(newJob);
        user.addJob(newJob);
        pendingJob++;
    }


    private ArrayList<JobReport_> handle_new_user(String[] cmd) {
        //REMOVE
        //System.out.println("User query");
        TrieNode<User> userNode = (TrieNode<User>)allUsers.search(cmd[1]);
        if (userNode==null)
            return null;

        int t1=Integer.parseInt(cmd[2]),t2=Integer.parseInt(cmd[3]);

        ArrayList<JobReport_> res=new ArrayList();
        ArrayList<Job>[] jobs=userNode.getValue().getJobs();

        int i,size=jobs[1].size();
        for (i=0;i<size;i++){
            Job currjob=jobs[1].get(i);
            //if (currjob.getArrTime()>t2)
            //    break;
            if (currjob.getArrTime()>=t1&&currjob.getArrTime()<=t2)
                res.add(new JobReport(cmd[1],currjob.getProject().toString(),currjob.getRuntime(),currjob.getArrTime(),currjob.getCompTime()));
            
        }
        size=jobs[0].size();
        for (i=0;i<size;i++){
            Job currjob=jobs[0].get(i); 
            if (currjob.getArrTime()>t2)
                break;
            else if (!currjob.completedStatus()&&currjob.getArrTime()>=t1&&currjob.getArrTime()<=t2)
                res.add(new JobReport(cmd[1],currjob.getProject().toString(),currjob.getRuntime(),currjob.getArrTime(),currjob.getCompTime()));
        }

        //REMOVE THIS
        //for (i=0;i<res.size();i++)
        //    System.out.println(res.get(i).user()+"\t"+res.get(i).project_name()+"\t"+res.get(i).budget()+"\t"+res.get(i).arrival_time()+"\t"+res.get(i).completion_time());

        return res;
    }

    private ArrayList<JobReport_> handle_new_projectuser(String[] cmd) {
        //REMOVE
        //System.out.println("Project User query");
        TrieNode<Project> projectNode = (TrieNode<Project>)allProjects.search(cmd[1]);
        if (projectNode==null)
            return null;

        TrieNode<User> userNode = (TrieNode<User>)allUsers.search(cmd[2]);
        if (userNode==null)
            return null;

        int t1=Integer.parseInt(cmd[3]),t2=Integer.parseInt(cmd[4]);

        ArrayList<JobReport_> res=new ArrayList();
        ArrayList<Job>[] jobs_proj=projectNode.getValue().getJobs();
        ArrayList<Job>[] jobs_user=userNode.getValue().getJobs();

        if (jobs_user[1].size()<jobs_proj[1].size()){
            int i,size=jobs_user[1].size();
            for (i=0;i<size;i++){
                Job currjob=jobs_user[1].get(i);
                //if (currjob.getArrTime()>t2)
                //    break;
                if (currjob.getArrTime()>=t1&&currjob.getArrTime()<=t2&&currjob.getProject().toString().equals(cmd[1]))
                    res.add(new JobReport(cmd[2],cmd[1],currjob.getRuntime(),currjob.getArrTime(),currjob.getCompTime()));
            }
        }
        else{
            int i,size=jobs_proj[1].size();
            for (i=0;i<size;i++){
                Job currjob=jobs_proj[1].get(i); 
                //if (currjob.getArrTime()>t2)
                //    break;
                if (currjob.getArrTime()>=t1&&currjob.getArrTime()<=t2&&currjob.getUser().toString().equals(cmd[2]))
                    res.add(new JobReport(cmd[2],cmd[1],currjob.getRuntime(),currjob.getArrTime(),currjob.getCompTime()));
            }
        }

        if (jobs_user[0].size()<jobs_proj[0].size()){
            int i,size=jobs_user[0].size();
            for (i=0;i<size;i++){
                Job currjob=jobs_user[0].get(i); 
                if (currjob.getArrTime()>t2)
                    break;
                else if (!currjob.completedStatus()&&currjob.getArrTime()>=t1&&currjob.getArrTime()<=t2&&currjob.getProject().toString().equals(cmd[1]))
                    res.add(new JobReport(cmd[2],cmd[1],currjob.getRuntime(),currjob.getArrTime(),currjob.getCompTime()));
            }
        }
        else{
            int i,size=jobs_proj[0].size();
            for (i=0;i<size;i++){
                Job currjob=jobs_proj[0].get(i); 
                if (currjob.getArrTime()>t2)
                    break;
                else if (!currjob.completedStatus()&&currjob.getArrTime()>=t1&&currjob.getArrTime()<=t2&&currjob.getUser().toString().equals(cmd[2]))
                    res.add(new JobReport(cmd[2],cmd[1],currjob.getRuntime(),currjob.getArrTime(),currjob.getCompTime()));
            }
        }

        //REMOVE THIS
        //for (int i=0;i<res.size();i++)
        //    System.out.println(res.get(i).user()+"\t"+res.get(i).project_name()+"\t"+res.get(i).budget()+"\t"+res.get(i).arrival_time()+"\t"+res.get(i).completion_time());

        return res;
    }

    private ArrayList<JobReport_> handle_new_project(String[] cmd) {
        //REMOVE
        //System.out.println("Project query");
        
        TrieNode<Project> projectNode = (TrieNode<Project>)allProjects.search(cmd[1]);
        if (projectNode==null)
            return null;

        int t1=Integer.parseInt(cmd[2]),t2=Integer.parseInt(cmd[3]);

        ArrayList<JobReport_> res=new ArrayList();
        ArrayList<Job>[] jobs=projectNode.getValue().getJobs();
        int i,size=jobs[1].size();
        for (i=0;i<size;i++){
            Job currjob=jobs[1].get(i); 
            //if (currjob.getArrTime()>t2)
            //    break;
            if (currjob.getArrTime()>=t1&&currjob.getArrTime()<=t2)
                res.add(new JobReport(currjob.getUser().toString(),cmd[1],currjob.getRuntime(),currjob.getArrTime(),currjob.getCompTime()));
        }
        size=jobs[0].size();
        for (i=0;i<size;i++){
            Job currjob=jobs[0].get(i); 
            if (currjob.getArrTime()>t2)
                break;
            else if (!currjob.completedStatus()&&currjob.getArrTime()>=t1&&currjob.getArrTime()<=t2)
                res.add(new JobReport(currjob.getUser().toString(),cmd[1],currjob.getRuntime(),currjob.getArrTime(),currjob.getCompTime()));
        }

        //REMOVE THIS
        //for (i=0;i<res.size();i++)
        //    System.out.println(res.get(i).user()+"\t"+res.get(i).project_name()+"\t"+res.get(i).budget()+"\t"+res.get(i).arrival_time()+"\t"+res.get(i).completion_time());

        return res;
    }

    @Override
    public ArrayList<UserReport_> timed_top_consumer(int top) {
        //REMOVE
        //System.out.println("Top query");
        ArrayList<UserReport_> res=new ArrayList();
        ArrayList<User> users=userConsumed.get();
        int size=users.size();
        for (int i=0;i<top&&i<size;i++)
            res.add(new UserReport(users.get(i).toString(),users.get(i).getBudget()));

        //REMOVE THIS
        //for (int i=0;i<res.size();i++)
        //    System.out.println(res.get(i).user()+"\t"+res.get(i).consumed());
        
        return res;
    }

    private ArrayList<JobReport_> handle_new_priority(String s) {
        //TAKE CARE OF PRIORITY OF PROJECTS
        //REMOVE
        //System.out.println("Priority query");
        ArrayList<Project> q=new ArrayList<Project>();
        ArrayList<JobReport_> res=new ArrayList();
        while (!projectsPriority.isEmpty()){
            Project p=projectsPriority.extractMax();
            if (p.getPriority()<Integer.parseInt(s)){
                q.add(p); //THIS IS IMP
                break;
            }
            ArrayList<Job> jobs=p.getJobs()[0];
            int size=jobs.size();
            for (int i=0;i<size;i++){
                Job j=jobs.get(i);
                if (!j.completedStatus())
                    res.add(new JobReport(j.getUser().toString(),p.toString(),j.getRuntime(),j.getArrTime(),j.getCompTime()));
            }
            q.add(p);
        }

        int si=q.size();
        for (int x=0;x<si;x++){
            projectsPriority.insert(q.get(x));
        }


        /*
        while (!q.isEmpty()){
            projectsPriority.insert(q.remove());
        }
        */

        
        //REMOVE THIS
        //for (int i=0;i<res.size();i++)
        //    System.err.println(res.get(i).user()+"\t"+res.get(i).project_name()+"\t"+res.get(i).budget()+"\t"+res.get(i).arrival_time()+"\t"+res.get(i).completion_time());

        return res;
    }

    public void handle_empty_line() {
       schedule();
       System.out.println("Execution cycle completed");
    }

    public void schedule() {
            execute_a_job();
    }



//*************************************************************************************************************************////
//*************************************************************************************************************************////
//*************************************************************************************************************************////
//*************************************************************************************************************************////
//*************************************************************************************************************************////
//*************************************************************************************************************************////
//*************************************************************************************************************************////
//*************************************************************************************************************************////

     //NOT CHANGED/CHECKED
    public void print_stats() {
        System.out.println("--------------STATS---------------");

        int count=completedJobs.size();
        System.out.println("Total jobs done: "+count);

        Job curr;
        while (!completedJobs.isEmpty()){
            curr=completedJobs.remove();
            System.out.println("Job{user=\'"+curr.getUser()+"\', project=\'"+curr.getProject()+"\', jobstatus=COMPLETED, execution_time="+curr.getRuntime()+", end_time="+curr.getCompTime()+", name=\'"+curr+"\'}");
        }
        System.out.println("------------------------");

        System.out.println("Unfinished jobs: ");
       
        /****MODIFYING THIS
        CHECK-------

        count=expensiveJobs.size();
        
        while (!expensiveJobs.isEmpty()){
            curr=expensiveJobs.remove();
            System.out.println("Job{user=\'"+curr.getUser()+"\', project=\'"+curr.getProject()+"\', jobstatus=REQUESTED, execution_time="+curr.getRuntime()+", end_time=null, name=\'"+curr+"\'}");
        }
        */

        count=0;
        while(!projectsPriority.isEmpty()){
            Project p=projectsPriority.extractMax();
            ArrayList<Job> jobs=p.getJobs()[0];
            int size=jobs.size();
            for (int i=0;i<size;i++){
                curr=jobs.get(i);
                if (!curr.completedStatus()){
                    count++;
                    System.out.println("Job{user=\'"+curr.getUser()+"\', project=\'"+curr.getProject()+"\', jobstatus=REQUESTED, execution_time="+curr.getRuntime()+", end_time=null, name=\'"+curr+"\'}");
                }
            }

        }
        System.out.println("Total unfinished jobs: "+count);
        System.out.println("--------------STATS DONE---------------");
    }







    public void handle_add(String[] cmd) {
        /***********************
        CAN BE MODIFIED:: CHECK
        just insert the expensive jobs of the project.. priority is taken care
        !!!!!!!!!!!!CHANGED VERIFY!!!!!!!!!!!!
        ************************/
        System.out.println("ADDING Budget");
        TrieNode<Project> getProj=(TrieNode)allProjects.search(cmd[1]);
        if (getProj==null){
            System.out.println("No such project exists. "+cmd[1]);
            return;
        }
        Project p=getProj.getValue();
        p.addBudget(Integer.parseInt(cmd[2]));

        //MaxHeap<Job> temp = new MaxHeap(); -- NOT REQ
        ArrayList<Job> eJobs=expensiveJobsUpd.removeVals(p.toString());

        /*
        //this has been changed... ---NOT REQ
        Queue<Job> temp2=new LinkedList();
        while (!expensiveJobs.isEmpty()){
            Job expensiveJob=expensiveJobs.remove();
            
            if (expensiveJob.getProject()!=getProj.getValue())
                temp2.add(expensiveJob);
        }
        expensiveJobs=temp2;
        */
        

        if (eJobs!=null){
            int size=eJobs.size();
            for (int i=0;i<size;i++){
                //Job expensiveJob=eJobs.get(i);
                //expensiveJob.unflagExpensive(); //THIS IS IMPORTANT!!!!!!!!!!
                allJobs.insert(eJobs.get(i));
                pendingJob++;
            }
        }

        /* --NOT REQ AT ALL
        while (!allJobs.isEmpty()){
            temp.insert(allJobs.extractMax());
        }
        allJobs=temp;
        */
    }



    public void run_to_completion() {
        while (!allJobs.isEmpty()){
            execute_a_job();
            System.out.println("System execution completed");
        }
    }


    public void execute_a_job() {
        System.out.println("Running code");
        System.out.println("\tRemaining jobs: "+pendingJob);
        while (true){
            Job currJob=allJobs.extractMax();
            if (currJob==null){
                return;
            }
            System.out.println("\tExecuting: "+currJob+" from: "+currJob.getProject());
            Project jobIn = currJob.getProject();
            pendingJob--;
            if (jobIn.getBudget()>=currJob.getRuntime()){
                completedJobs.add(currJob);
                currTime+=currJob.getRuntime();
                currJob.flagCompleted();
                currJob.setCompTime(currTime);
                jobIn.addBudget(0-currJob.getRuntime());
                currJob.getUser().addBudget(currJob.getRuntime());
                userConsumed.update(currJob.getUser());
                currJob.getUser().completeJob(currJob);
                jobIn.completeJob(currJob);
                System.out.println("\tProject: "+jobIn+" budget remaining: "+jobIn.getBudget());
                return;
            }
            System.out.println("\tUn-sufficient budget.");
            //currJob.flagExpensive();
            //expensiveJobs.add(currJob);
            expensiveJobsUpd.insert(currJob.getProject().toString(),currJob);
        }
    }


    @Override
    public void timed_flush(int waittime) {
        //SOME STATS can be pushed to some other functions to reduce time...
        //REMOVE (all prints)

        int temp_token=0;
        //ArrayList<Wrap<Job>> temp=new ArrayList();
        MaxHeap<Job> temp=new MaxHeap<Job>();
        int time=currTime;

        while (!allJobs.isEmpty()){
            Job currJob=allJobs.extractMax();
         
            //REMOVE   
            //System.err.println(currJob+currJob.get);
            if (time-currJob.getArrTime()>=waittime&&currJob.getProject().getBudget()>=currJob.getRuntime()){
                    Project jobIn = currJob.getProject();
                    pendingJob--;
                    
                    
                    completedJobs.add(currJob);
                    currTime+=currJob.getRuntime();
                    currJob.flagCompleted();
                    currJob.setCompTime(currTime);
                    jobIn.addBudget(0-currJob.getRuntime());
                    currJob.getUser().addBudget(currJob.getRuntime());
                    userConsumed.update(currJob.getUser());
                    currJob.getUser().completeJob(currJob);
                    jobIn.completeJob(currJob);
                    //System.out.println("\tProject: "+jobIn+" budget remaining: "+jobIn.getBudget());                
            }
            else{
                //temp.add(new Wrap<Job>(currJob,temp_token++));
                temp.insert(currJob);
            }
            
        }
        //allJobs=new MaxHeap<Job>(temp);
        allJobs=temp;
    

    }



    public void timed_run_to_completion(){

            while (!allJobs.isEmpty()){
                Job currJob=allJobs.extractMax();

                Project jobIn = currJob.getProject();
                pendingJob--; //REMOVE??
                if (jobIn.getBudget()>=currJob.getRuntime()){
                    completedJobs.add(currJob); //REQ!!!!
                    currTime+=currJob.getRuntime(); // REQ!!
                    currJob.flagCompleted();        //REMOVE??
                    currJob.setCompTime(currTime);  // REQ!!
                    jobIn.addBudget(0-currJob.getRuntime()); //REQ!!!
                    //currJob.getUser().addBudget(currJob.getRuntime()); //REMOVE?? 
                    //userConsumed.update(currJob.getUser()); //REMOVE??
                    //((TrieNode<User>)allUsers.search(currJob.getUser().toString())).getValue().completeJob(currJob); //REMOVE??
                    //((TrieNode<Project>)allProjects.search(currJob.getProject().toString())).getValue().completeJob(currJob); //REMOVE??
                }
                //else{
                    //currJob.flagExpensive();
                    //expensiveJobs.add(currJob); -- NOT REQ
                    //expensiveJobsUpd.insert(currJob.getProject().toString(),currJob);
                //}
            }       
        
    }

    

}





//Interface Implementations

class JobReport implements JobReport_{
    String User,ProjectName;
    int Budget,ArrTime,CompTime;

    public JobReport(String User,String ProjectName, int Budget, int ArrTime, int CompTime){
        this.User=User;
        this.ProjectName=ProjectName;
        this.Budget=Budget;
        this.ArrTime=ArrTime;
        this.CompTime=CompTime;
    }

    public String user()            {   return User;        }
    public String project_name()    {   return ProjectName; }
    public int budget()             {   return Budget;      }
    public int arrival_time()       {   return ArrTime;     }
    public int completion_time()    {   return CompTime;    }
}


class UserReport implements UserReport_{
    String User;
    int Consumed;

    public UserReport(String User, int Consumed){
        this.User=User;
        this.Consumed=Consumed;
    }

    public String user()    {   return User;        }
    public int consumed()   {   return Consumed;    }
}

class PriorityListUsers{
    ArrayList<User> arr=new ArrayList();

    public void add(User obj){  
        arr.add(obj);
    }

    public void update(User obj){
        int index=arr.indexOf(obj),budget=obj.getBudget();
        int i=index-1;
        while (i>=0&&budget>arr.get(i).getBudget()){
            arr.set(i+1,arr.get(i));
            i--;
        }
        arr.set(i+1,obj);
    }
    /*

    public void updateBulk(ArrayList<User> objs){
        int index=arr.indexOf(obj),budget=obj.getBudget();
        int i=index-1;
        while (i>=0&&budget>arr.get(i).getBudget()){
            arr.set(i+1,arr.get(i));
            i--;
        }
        arr.set(i+1,obj);
    }
*/
    public ArrayList<User> get(){
        return arr;
    }
}

/*
class PriorityListProjects{
    class MyLL{
        MyNode root=null;


        class MyNode{
            MyNode next;
            Project val;

            public MyNode(MyNode after, Project value){
                next=after;
                val=value;
            }

            public MyNode(Project value){
                next=null;
                val=value;
            }
        }

        public void add(Project obj){
            if (root==null)
                root=new MyNode(obj);
            else{
                MyNode par=null,curr=root;
                int priority=obj.getPriority();
                while (curr!=null&&curr.val.getPriority()>=priority){
                    par=curr;
                    curr=curr.next;
                }
                if (par==null)
                    root=new MyNode(root,obj);
                else
                    par.next=new MyNode(curr,obj);
            }
        }

    }

    MyLL arr1=new MyLL();
    ArrayList<Project> arr=new ArrayList();
    long t1=0,t2=0;

    public void add(Project obj){
        long qstart_time = System.nanoTime();
            arr1.add(obj);   
        long qend_time = System.nanoTime();
        t1+=(qend_time - qstart_time);
        System.out.println("Time elapsed (ns): "+(qend_time - qstart_time)+"\t" + t1);
        
        
        qstart_time = System.nanoTime();
            arr.add(obj);

            int index=arr.indexOf(obj);
            int priority=obj.getPriority();
            int i=index-1;
            while (i>=0&&priority>arr.get(i).getPriority()){
                arr.set(i+1,arr.get(i));
                i--;
            }
            arr.set(i+1,obj);
        qend_time = System.nanoTime();
        t2+=(qend_time - qstart_time);
        System.out.println("Time elapsed (ns): "+(qend_time - qstart_time)+"\t" + t2);
        
        
    }

    public ArrayList<Project> get(){
        return arr;
    }

}

*/

