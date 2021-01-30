package group2.connectsentinel.datamanagement;

import group2.connectsentinel.data.Report;

public interface ReportDataSource {

    // false if the same message already exists.
    // true if successfully added
    boolean addReport(Report report);

    int unhandledReportCount();

    boolean hasUnhandledReport();
    Report peekFirstUnhandledReport();
    Report popFirstUnhandledReport();

}
