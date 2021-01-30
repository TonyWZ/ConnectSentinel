package group2.connectsentinel.datamanagement;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import group2.connectsentinel.data.Report;

// TODO:Sylvie Implement!!
// Like the Fake User Data, Singleton design is used here.
public class FakeReportData implements ReportDataSource {

    private Queue<Report> unhandledReport;
    private Set<Report> handledReport;
    private static final ReportDataSource fakeReportDB = new FakeReportData();

    private FakeReportData() {
        unhandledReport = new LinkedList<Report>();
        handledReport = new HashSet<Report>();
    }

    public static ReportDataSource getInstance(){
        return fakeReportDB;
    }

    @Override
    public int unhandledReportCount() {
        return unhandledReport.size();
    }

    @Override
    public boolean addReport(Report report) {
        return unhandledReport.add(report);
    }

    @Override
    public boolean hasUnhandledReport() {
        return !unhandledReport.isEmpty();
    }

    @Override
    public Report peekFirstUnhandledReport() {
        return unhandledReport.peek();
    }

    @Override
    public Report popFirstUnhandledReport() {
        Report rep = unhandledReport.poll();
        handledReport.add(rep);
        return rep;
    }
}
