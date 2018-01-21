package com.alibaba.analysis.metrics.calculator.linesofcode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.analysis.metrics.calculator.AbstractASTVisitorCalculator;
import com.alibaba.analysis.metrics.metric.MetricId;
import com.alibaba.analysis.metrics.util.ValidateJava;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// LinesOfCodeCalculator3.java和LinesOfCodeCalculator4.java差别太大也识别不出来
public class LinesOfCodeCalculator extends AbstractASTVisitorCalculator{
    public static final MetricId TOTAL_LINES_OF_CODE = MetricId.create(LinesOfCodeCalculator.class, "tloc");
    public static final MetricId SOURCE_LINES_OF_CODE = MetricId.create(LinesOfCodeCalculator.class, "sloc");
    public static final MetricId BLANK_LINES_OF_CODE = MetricId.create(LinesOfCodeCalculator.class, "bloc");
    public static final MetricId COMMENT_LINES_OF_CODE = MetricId.create(LinesOfCodeCalculator.class, "cloc");
    public static final MetricId LINE_OF_COMMENTTED_CODE = MetricId.create(LinesOfCodeCalculator.class, "locc");

    Logger LOG = LoggerFactory.getLogger(LinesOfCodeCalculator.class);


    private static Pattern line = Pattern.compile("\r\n|\r|\n");
    private String source;
    private List<String> comments;
    private List<CommentType> commentTypes;
    private int tloc, sloc, cloc, bloc, locc;

    public static int countLines(String str) {
        if(str == null || str.isEmpty())
        {
            return 0;
        }
        Matcher m = line.matcher(str);
        int lines = 1;
        while (m.find())
        {
            lines ++;
        }
        return lines;
    }

    private void calNumOfLines(){
        File file = getContext().getFile();
        comments = new ArrayList<>();
        commentTypes = new ArrayList<>();
        try {
            source = new String(getContext().getUnparsed());
            tloc = countLines(source);
            // 去除空白行
            String removeBlank = source.trim().replaceAll("(?m)^[ \t]*\r?\n", "");
            bloc = tloc - countLines(removeBlank);

            for ( Comment comment : (List<Comment>)getContext().getParsedCompilationUnit().getCommentList()) {
                comment.accept(new CommentVisitor(new String(getContext().getUnparsed())));
            }
            //去除注释
            String removeComment = source;
            // 按照行数由长到短排序，然后依次替换，否则如果长的注释包含了短的注释，
            // 替换短的注释会把长的注释破坏掉，计算不准确
            Collections.sort(comments, new StringLengthComparator());
            for (String comment : comments) {
                removeComment = removeComment.replace(comment, "");
            }
            // 去除空白行，包括本来存在的空白行以及由于删除注释而产生的空白行
            sloc = countLines(removeComment.trim().replaceAll("(?m)^[ \t]*\r?\n", ""));
            // 注释掉的代码
            String removeCommentNotCode = source;
            for (int i = 0; i < comments.size(); i++) {
                String comment = comments.get(i);
                if (commentTypes.get(i).equals(CommentType.JavaDoc)) {
                    removeCommentNotCode = removeCommentNotCode.replace(comment, "");
                    continue;
                }
                String[] lines = comment.split("\r\n?|\n");
                boolean isCommenttedCode = false;
                for (String line : lines) {
                    if (ValidateJava.isJava(line)) {
                        isCommenttedCode = true;
                        break;
                    }
                }
                // 如果不是注释掉的代码，才进行替换
                if (!isCommenttedCode) {
                    removeCommentNotCode = removeCommentNotCode.replace(comment, "");
                }
            }
            // 新增的行数，就是没有被删掉的注释掉的代码
            locc = countLines(removeCommentNotCode.trim().replaceAll("(?m)^[ \t]*\r?\n", "")) - sloc;
            cloc = tloc - sloc - bloc - locc;
        } catch (StackOverflowError error){
            LOG.error("Calculate SLOC on {} throw Stack overflow.", file.getAbsolutePath());
            bloc = -1;
            cloc = -1;
            sloc = -1;
            locc = -1;
        }
        noteFileValue(TOTAL_LINES_OF_CODE, tloc);
        noteFileValue(SOURCE_LINES_OF_CODE, sloc);
        noteFileValue(BLANK_LINES_OF_CODE, bloc);
        noteFileValue(COMMENT_LINES_OF_CODE, cloc);
        noteFileValue(LINE_OF_COMMENTTED_CODE, locc);
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        calNumOfLines();
        return false;
    }

    @Override
    public boolean visit(EnumDeclaration node) {
        super.visit(node);
        calNumOfLines();
        return false;
    }

    class CommentVisitor extends ASTVisitor {
        private String s;

        CommentVisitor(String s) {
            this.s = s;
        }

        @Override
        public boolean visit(LineComment node) {
            int start = node.getStartPosition();
            int end = start + node.getLength();
            String comment = s.substring(start, end);
            //LOG.debug(comment);
            comments.add(comment);
            commentTypes.add(CommentType.Line);
            return true;
        }

        @Override
        public boolean visit(BlockComment node) {
            int start = node.getStartPosition();
            int end = start + node.getLength();
            String comment = s.substring(start, end);
            //LOG.debug(comment);
            comments.add(comment);
            commentTypes.add(CommentType.Block);
            return true;
        }

        @Override
        public boolean visit(Javadoc node) {
            int start = node.getStartPosition();
            int end = start + node.getLength();
            String comment = s.substring(start, end);
            //LOG.debug(comment);
            comments.add(comment);
            commentTypes.add(CommentType.JavaDoc);
            return true;
        }

    }

    class StringLengthComparator implements java.util.Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            return - (s1.length() - s2.length());
        }
    }

    enum CommentType {
        /**
         * JavaDoc
         */
        JavaDoc,
        /**
         * 块注释
         */
        Block,
        /**
         * 行注释
         */
        Line
    }

}
