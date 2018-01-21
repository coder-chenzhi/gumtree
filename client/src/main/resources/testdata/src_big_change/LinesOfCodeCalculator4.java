package com.alibaba.analysis.metrics.calculator.linesofcode;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.analysis.metrics.calculator.AbstractASTVisitorCalculator;
import com.alibaba.analysis.metrics.metric.MetricId;
import com.alibaba.analysis.metrics.util.ValidateJava;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//LinesOfCodeCalculator3.java和LinesOfCodeCalculator4.java差别太大也识别不出来
public class LinesOfCodeCalculator extends AbstractASTVisitorCalculator{
    public static final MetricId TOTAL_LINES_OF_CODE = MetricId.create(LinesOfCodeCalculator.class, "tloc");
    public static final MetricId SOURCE_LINES_OF_CODE = MetricId.create(LinesOfCodeCalculator.class, "sloc");
    public static final MetricId BLANK_LINES_OF_CODE = MetricId.create(LinesOfCodeCalculator.class, "bloc");
    public static final MetricId COMMENT_LINES_OF_CODE = MetricId.create(LinesOfCodeCalculator.class, "cloc");
    public static final MetricId LINE_OF_COMMENTED_CODE = MetricId.create(LinesOfCodeCalculator.class, "commentedCode");
    public static final MetricId LINE_OF_USEFUL_COMMENT = MetricId.create(LinesOfCodeCalculator.class, "usefulComment");

    Logger LOG = LoggerFactory.getLogger(LinesOfCodeCalculator.class);

    private String source;
    private String[] lines;
    // 注释所在行数
    private Set<Integer> commentLineNum;
    // 行中注释所在行数
    private Set<Integer> commentInsideLineNum;
    // 有效注释所在行数
    private Set<Integer> usefulCommentLineNum;
    // 注释掉的代码所在行数
    private Set<Integer> commentedCodeLineNum;
    // 文件总行数
    private int tloc;
    // 有效代码行数
    private int sloc;
    // 空白行数
    private int bloc;
    // 注释行数
    private int cloc;
    // 有效注释行数
    private int usefulComment;
    // 注释掉的代码行数
    private int commentedCode;
    // 文件头注释，一般是版权信息
    private int file_header_comment;


    private void init() {
        commentLineNum = new HashSet<>();
        commentInsideLineNum = new HashSet<>();
        usefulCommentLineNum = new HashSet<>();
        commentedCodeLineNum = new HashSet<>();
        tloc = 0;
        bloc = 0;
        cloc = 0;
        sloc = 0;
        usefulComment = 0;
        commentedCode = 0;
        file_header_comment = 0;
    }


    private void calNumOfLines(){
        File file = getContext().getFile();
        init();
        try {
            source = new String(getContext().getUnparsed());
            lines = source.split("\r\n|\r|\n", -1);

            tloc = countLines(source);
            bloc = (int)Arrays.asList(lines).stream().map(String::trim).filter(String::isEmpty).count();

            for ( Comment comment : (List<Comment>)getContext().getParsedCompilationUnit().getCommentList()) {
                comment.accept(new CommentVisitor(
                    getContext().getParsedCompilationUnit(), new String(getContext().getUnparsed())));
            }
            cloc = commentLineNum.size();
            usefulComment = usefulCommentLineNum.size();
            commentedCode = commentedCodeLineNum.size();
            sloc = tloc - bloc - cloc + commentInsideLineNum.size();
        } catch (StackOverflowError error){
            LOG.error("Calculate SLOC on {} throw Stack overflow.", file.getAbsolutePath());
            bloc = -1;
            cloc = -1;
            sloc = -1;
            usefulComment = -1;
            commentedCode = -1;
        }
        noteFileValue(TOTAL_LINES_OF_CODE, tloc);
        noteFileValue(SOURCE_LINES_OF_CODE, sloc);
        noteFileValue(BLANK_LINES_OF_CODE, bloc);
        noteFileValue(COMMENT_LINES_OF_CODE, cloc);
        noteFileValue(LINE_OF_COMMENTED_CODE, commentedCode);
        noteFileValue(LINE_OF_USEFUL_COMMENT, usefulComment);
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
        private CompilationUnit parsed;
        private String s;


        CommentVisitor(CompilationUnit parsed, String s) {
            this.parsed = parsed;
            this.s = s;
        }

        protected int getStartLineNumber(final ASTNode node) {
            return getLineNumber(node.getStartPosition());
        }

        protected int getEndLineNumber(final ASTNode node) {
            return getLineNumber(node.getStartPosition() + node.getLength());
        }

        protected int getLineNumber(final int position) {
            return parsed.getLineNumber(position);
        }

        @Override
        public boolean visit(LineComment node) {
            return calculateComment(node);
        }

        @Override
        public boolean visit(BlockComment node) {
            return calculateComment(node);
        }

        @Override
        public boolean visit(Javadoc node) {
            return calculateComment(node);
        }

        private boolean calculateComment(ASTNode node) {
            int start = node.getStartPosition();
            int end = start + node.getLength();
            String comment = s.substring(start, end);
            handleInsideComment(node, comment);
            if (getStartLineNumber(node) == file_header_comment + 1) {
                file_header_comment = getEndLineNumber(node);
                return true;
            }
            if (handleCommentedCode(node, comment)) {
                return true;
            }
            handleOrdinaryComment(node, comment);
            return true;
        }

        private void handleInsideComment(ASTNode node, String comment) {
            int startLineNum = getStartLineNumber(node);
            int endLineNum = getEndLineNumber(node);
            String[] commentLines = comment.split("\r\n|\r|\n");
            String startComment = commentLines[0];
            String endComment = commentLines[commentLines.length - 1];
            if (!lines[startLineNum-1].replace(startComment, "").trim().isEmpty()) {
                commentInsideLineNum.add(startLineNum);
            }
            if (!lines[endLineNum-1].replace(endComment, "").trim().isEmpty()) {
                commentInsideLineNum.add(endLineNum);
            }
        }

        private void handleOrdinaryComment(ASTNode node, String comment) {
            int startLineNum = getStartLineNumber(node);
            int endLineNum = getEndLineNumber(node);
            for (int i = startLineNum; i < endLineNum + 1; i++) {
                commentLineNum.add(i);
            }
            String[] commentLines = comment.split("\r\n|\r|\n");
            for (int i = 0; i < commentLines.length; i++) {
                if (commentLines[i].matches("(.)*\\p{L}+(.)*")) {
                    usefulCommentLineNum.add(startLineNum + i);
                }
            }
        }

        /**
         * 检查是否是注释掉的代码，如果是JavaDoc直接返回false
         * @param node 节点
         * @param comment 注释内容
         * @return 是否是注释掉的代码
         */
        private boolean handleCommentedCode(ASTNode node, String comment) {
            if (node instanceof Javadoc) {
                return false;
            }
            int startLineNum = getStartLineNumber(node);
            String[] commentLines = comment.split("\r\n|\r|\n");
            int codeLine = 0;
            for (String line : commentLines) {
                if (ValidateJava.isJava(line)) {
                    codeLine++;
                }
            }
            if (1.0 * codeLine / commentLines.length >= 0.5) {
                for (int i = 0; i < commentLines.length; i++) {
                    commentedCodeLineNum.add(startLineNum + i);
                }
                return true;
            } else {
                return false;
            }
        }

    }


    public static int countLines(String str) {
        if(str == null || str.isEmpty())
        {
            return 0;
        }
        return str.split("\r\n|\r|\n", -1).length;
    }

}
