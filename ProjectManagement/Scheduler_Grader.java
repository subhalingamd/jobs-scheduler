package ProjectManagement;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Scheduler_Grader {
    private static Scheduler_Driver scheduler_driver;

    private static ArrayList<Long> user_time, job_time, project_time, rtc_time, job_report_time, user_report_time, flush_time;

    static PrintStream o;
    static PrintStream console;
    static Boolean overallsuccess = true;

    public static void main(String[] args) throws FileNotFoundException {

        user_time = new ArrayList<>();
        job_time = new ArrayList<>();
        project_time = new ArrayList<>();
        rtc_time = new ArrayList<>();
        job_report_time = new ArrayList<>();
        user_report_time = new ArrayList<>();
        flush_time = new ArrayList<>();

        // Creating a File object that represents the disk file.
        o = new PrintStream(new File("tmp_output"));
        // Store current System.out before assigning a new value
        console = System.out;
        // Assign o to output stream
        Boolean debug = true;
        debug = false;
        if (!debug)
            System.setOut(o);

        scheduler_driver = new Scheduler_Driver();
        File file;
        if (args.length == 0) {
            URL url = Scheduler_Driver.class.getResource("INP");
            file = new File(url.getPath());
        } else {
            file = new File(args[0]);
        }

        try {
            execute(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!debug) {
            System.setOut(console);
            System.out.println();
            System.out.println("------------");
            System.out.println("Queries ran successfully");
            if(overallsuccess)
                System.out.println("Partial validation was successful.");
            else
                System.out.println("Partial validation was NOT successful.");

            System.out.println("(Validation only checks CORRECTNESS and NOT COMPLETENESS.)");

            System.out.println("Job report average time(ms): " + calculateAverage(job_report_time));
            System.out.println("User report average time (ms): " + calculateAverage(user_report_time));
            System.out.println("Flush report average time (ms): " + calculateAverage(flush_time));
            System.out.println();
        }
    }

    static void execute(File commandFile) throws IOException {


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
                        scheduler_driver.timed_handle_project(cmd);
                        break;
                    case "JOB":
                        scheduler_driver.timed_handle_job(cmd);
                        break;
                    case "USER":
                        scheduler_driver.timed_handle_user(cmd[1]);
                        break;
                    case "QUERY":
                        scheduler_driver.handle_query(cmd[1]);
                        break;
                    case "":
                        scheduler_driver.handle_empty_line();
                        break;
                    case "ADD":
                        scheduler_driver.handle_add(cmd);
                        break;
                    //--------- New Queries
                    case "NEW_USER":
                    case "NEW_PROJECT":
                    case "NEW_PROJECTUSER":
                    case "NEW_PRIORITY":
                        qstart_time = System.currentTimeMillis();
                        ArrayList<JobReport_> out_user = scheduler_driver.timed_report(cmd);
                        qend_time = System.currentTimeMillis();
                        System.out.println("Time elapsed (ns): " + (qend_time - qstart_time));
                        job_report_time.add(qend_time - qstart_time);
                        validate_timed_report(out_user, cmd);
                        break;

                    case "NEW_TOP":
                        qstart_time = System.currentTimeMillis();
                        ArrayList<UserReport_> out_topuser = scheduler_driver.timed_top_consumer(Integer.parseInt(cmd[1]));
                        qend_time = System.currentTimeMillis();
                        System.out.println("Time elapsed (ns): " + (qend_time - qstart_time));
                        user_report_time.add(qend_time - qstart_time);

                        break;
                    case "NEW_FLUSH":
                        qstart_time = System.currentTimeMillis();
                        scheduler_driver.timed_flush(Integer.parseInt(cmd[1]));
                        qend_time = System.currentTimeMillis();
                        flush_time.add(qend_time - qstart_time);
                        break;
                    default:
                        System.err.println("Unknown command: " + cmd[0]);
                }
            }


            scheduler_driver.timed_run_to_completion();
            scheduler_driver.print_stats();

        } catch (FileNotFoundException e) {
            System.err.println("Input file Not found. " + commandFile.getAbsolutePath());
        } catch (NullPointerException ne) {
            ne.printStackTrace();

        }
    }

    private static void validate_timed_report(ArrayList<JobReport_> out, String[] cmd) {
        System.setOut(console);
        String project_name, user_name;
        Integer start_time, end_time;
        Boolean success = true;
        System.out.print("Validating: ");
        switch (cmd[0]) {
            case "NEW_PROJECT":
                System.out.print("Project query");
                project_name = cmd[1].trim();
                start_time = Integer.parseInt(cmd[2]);
                end_time = Integer.parseInt(cmd[3]);
                System.out.print(". Expecting Arrival time between: " + start_time + " and " + end_time + " and belongs to: " + project_name);
                for (JobReport_ jobReport_ : out) {
                    if (jobReport_.arrival_time() < start_time || jobReport_.arrival_time() > end_time ||
                            jobReport_.project_name().compareToIgnoreCase(project_name.trim()) != 0) {
                        System.out.println(" FAILED");
                        success = false;
                        break;
                    }

                }
                if (success)
                    System.out.println(" SUCCESS");
                break;
            case "NEW_USER":
                System.out.print("User query");
                user_name = cmd[1].trim();
                start_time = Integer.parseInt(cmd[2]);
                end_time = Integer.parseInt(cmd[3]);
                System.out.print(". Expecting Arrival time between: " + start_time + " and " + end_time + " and belongs to: " + user_name);

                for (JobReport_ jobReport_ : out) {
                    if (jobReport_.arrival_time() < start_time || jobReport_.arrival_time() > end_time ||
                            jobReport_.user().compareToIgnoreCase(user_name.trim()) != 0) {
                        System.out.println(" FAILED");
                        success = false;
                        break;
                    }

                }
                if (success)
                    System.out.println(" SUCCESS");
                break;
            case "NEW_PROJECTUSER":
                System.out.print("Project User query");
                project_name = cmd[1].trim();
                user_name = cmd[2].trim();
                start_time = Integer.parseInt(cmd[3]);
                end_time = Integer.parseInt(cmd[4]);
                System.out.print(". Expecting Arrival time between: " + start_time + " and " + end_time + " and belongs to: " + project_name + ", " + user_name);
                for (JobReport_ jobReport_ : out) {
                    if (jobReport_.arrival_time() < start_time || jobReport_.arrival_time() > end_time
                            ||jobReport_.project_name().compareToIgnoreCase(project_name.trim()) != 0 || jobReport_.user().compareToIgnoreCase(user_name.trim()) != 0) {
                        System.out.println(" FAILED");
                        success = false;
                        break;
                    }

                }
                if (success)
                    System.out.println(" SUCCESS");
                break;
            case "NEW_PRIORITY":
//                System.out.print("Priority query");
//                Integer priority = Integer.parseInt(cmd[1]);
//                System.out.print(". Expecting Priority: " + priority + "  or more");
////                for (JobReport_ jobReport_ : out) {
////                    if (jobReport_.priority() < priority) {
////                        System.out.println(" FAILED");
////                        success = false;
////                        break;
////                    }
////
////                }
//                if (success)
//                    System.out.println(" NOT CHECKED");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + cmd[0]);
        }

        System.setOut(o);
    }

    private static double calculateAverage(List<Long> longs) {
        Long sum = 0l;
        if (!longs.isEmpty()) {
            for (Long mark : longs) {
                sum += mark;
            }
            return sum.doubleValue() / longs.size();
        }
        return sum;
    }
}
