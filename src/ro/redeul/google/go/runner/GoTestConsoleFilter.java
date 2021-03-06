package ro.redeul.google.go.runner;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GoTestConsoleFilter implements Filter {
    private static final Pattern MSG_LINE = Pattern.compile("^\t(.*):(\\d+):.*\n?");

    private final Project project;
    private final String packageDir;

    public GoTestConsoleFilter(Project project, String packageDir) {
        this.project = project;
        this.packageDir = packageDir;
    }

    @Override
    public Result applyFilter(String line, int entireLength) {
        Matcher matcher = MSG_LINE.matcher(line);
        if (!matcher.matches()) {
            return null;
        }

        String fileName = matcher.group(1);
        int fileLine;
        try {
            fileLine = Integer.parseInt(matcher.group(2)) - 1;
        } catch (NumberFormatException e) {
            return null;
        }

        VirtualFile vf = project.getBaseDir().findFileByRelativePath(packageDir + "/" + fileName);
        if (vf == null) {
            return null;
        }

        HyperlinkInfo hyperlinkInfo = new OpenFileHyperlinkInfo(project, vf, fileLine);
        int outputStart = entireLength - line.length();
        return new Result(outputStart + matcher.start(1), outputStart + matcher.end(2), hyperlinkInfo);
    }
}
