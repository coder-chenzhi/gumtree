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

/**
 * LinesOfCodeCalculator1.java和LinesOfCodeCalculator2.java中的类名如果不匹配的话，就什么都识别不出来了
 * 把LinesOfCodeCalculator改成其他
 * @author chenzhi
 *
 */
public class LinesOfCodeCalculator extends AbstractASTVisitorCalculator{
    public static final MetricId TOTAL_LINES_OF_CODE = MetricId.create(LinesOfCodeCalculator.class, "tloc");
    public static final MetricId SOURCE_LINES_OF_CODE = MetricId.create(LinesOfCodeCalculator.class, "sloc");
    public static final MetricId BLANK_LINES_OF_CODE = MetricId.create(LinesOfCodeCalculator.class, "bloc");
    public static final MetricId COMMENT_LINES_OF_CODE = MetricId.create(LinesOfCodeCalculator.class, "cloc");
    public static final MetricId LINE_OF_COMMENTED_CODE = MetricId.create(LinesOfCodeCalculator.class, "commentedCode");
    public static final MetricId LINE_OF_USEFUL_COMMENT = MetricId.create(LinesOfCodeCalculator.class, "usefulComment");

}
