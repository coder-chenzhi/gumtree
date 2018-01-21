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

}
