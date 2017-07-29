package jp.gr.naoco.db.sql;

import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.gr.naoco.db.exception.QueryTemplateException;
import jp.gr.naoco.db.sql.TemplateReader.TemplateLineIterator;
import jp.gr.naoco.db.sql.elem.CommentElem;
import jp.gr.naoco.db.sql.elem.EoqElem;
import jp.gr.naoco.db.sql.elem.ForeachElem;
import jp.gr.naoco.db.sql.elem.IfElem;
import jp.gr.naoco.db.sql.elem.QueryBodyElem;
import jp.gr.naoco.db.sql.elem.SqlElem;
import jp.gr.naoco.db.sql.elem.VariableElem;

public class TemplateAnalyzer {

    protected final static Map<String, SqlElem> ELEM_CACHE = new ConcurrentHashMap<String, SqlElem>();

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constants

    public static final int INDEX_NOT_FOUND = -1;

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Methods

    public static SqlElem analyze(String templatePath, Iterator<String> templateLines) {
        SqlElem firstElem = ELEM_CACHE.get(templatePath);
        if (null == firstElem) {
            firstElem = analyzeComment(templateLines);
            firstElem = analyzeVariable(null, firstElem);
            firstElem = analyzeIf(null, firstElem);
            firstElem = analyzeForeach(null, firstElem);
            ELEM_CACHE.put(templatePath, firstElem);
        } else {
            if ((null != templateLines) && (templateLines instanceof TemplateLineIterator)) {
                templateLines.remove();
            }
        }
        return firstElem;
    }

    // ///////////////////////

    private static SqlElem analyzeComment(Iterator<String> templateLines) {
        StringBuilder query = new StringBuilder();
        StringBuilder comment = new StringBuilder();
        SqlElem firstElem = new QueryBodyElem("");
        SqlElem prevElem = firstElem;
        boolean innerComment = false;
        while (templateLines.hasNext()) {
            String line = templateLines.next();
            line = line.trim();
            line = " " + line;
            innerComment = analyzeCommentOneLine(line, prevElem, query, comment, innerComment);
            while (!prevElem.isLast()) {
                prevElem = prevElem.getNext();
            }
        }
        if (0 < query.length()) {
            prevElem.setNext(new QueryBodyElem(query.toString().trim()));
        } else if (0 < comment.length()) {
            prevElem.setNext(new CommentElem(comment.toString()));
        }
        return firstElem.getNext();
    }

    private static boolean analyzeCommentOneLine(String line, SqlElem prevElem, StringBuilder query,
            StringBuilder comment, boolean innerComment) {
        // 複数行コメント中の場合
        if (innerComment) {
            int endIndex = line.indexOf("*/");
            if (endIndex < 0) {
                comment.append(line);
                return true;
            }
            String commentPart = line.substring(0, endIndex);
            comment.append(commentPart);
            if (null != prevElem) {
                SqlElem current = new CommentElem(comment.toString());
                prevElem.setNext(current);
                return analyzeCommentOneLine(line.substring(endIndex + 2), current, query,
                        comment.delete(0, comment.length()), false);
            }
        }

        int multiEnd = line.indexOf("/*");
        int singleEnd = line.indexOf("--");

        // コメントが含まれない場合
        if ((multiEnd < 0) && (singleEnd < 0)) {
            query.append(line);
            return false;
        }

        // 複数行コメント
        if (((singleEnd < 0) || (multiEnd < singleEnd)) && (0 <= multiEnd)) {
            String queryBody = line.substring(0, multiEnd);
            query.append(queryBody);
            SqlElem current = new QueryBodyElem(query.toString().trim());
            prevElem.setNext(current);
            return analyzeCommentOneLine(line.substring(multiEnd + 2), current, query.delete(0, query.length()),
                    comment, true);
        }

        // 一行コメント
        String queryBody = line.substring(0, singleEnd);
        query.append(queryBody);
        SqlElem current = new QueryBodyElem(query.toString().trim());
        query.delete(0, query.length());
        prevElem.setNext(current);
        current.setNext(new CommentElem(line.substring(singleEnd + 2)));
        return false;
    }

    // ///////////////////////

    private static SqlElem analyzeVariable(SqlElem prevElem, SqlElem currentElement) {
        SqlElem nextElment = currentElement.getNext();
        if (currentElement instanceof QueryBodyElem) {

            StringTokenizer st = new StringTokenizer(((QueryBodyElem) currentElement).getValue(), "#", true);
            boolean isVariable = false;
            boolean isFirst = true;
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if ("#".equals(token)) {
                    isVariable = !isVariable;
                    continue;
                }

                if (isVariable && token.isEmpty()) {
                    throw new QueryTemplateException("variable key is empty");
                }

                SqlElem newElem = (isVariable ? new VariableElem(token) : new QueryBodyElem(token + " "));
                if (isFirst) {
                    currentElement = newElem;
                }
                if (null != prevElem) {
                    prevElem.setNext(newElem);
                }
                prevElem = newElem;
                isFirst = false;
            }
            if (null != prevElem) {
                prevElem.setNext(nextElment);
            }
        }
        if (!currentElement.isLast()) {
            analyzeVariable(currentElement, currentElement.getNext());
        }
        return currentElement;
    }

    // ///////////////////////

    private static SqlElem analyzeIf(SqlElem prevElem, SqlElem currentElem) {
        if (currentElem instanceof CommentElem) {
            String comment = ((CommentElem) currentElem).getComment();
            Pattern pattern = Pattern.compile("^ IF #(.*)#");
            Matcher matcher = pattern.matcher(comment);
            if (matcher.find()) {
                String key = matcher.group(1);
                IfElem ifElem = new IfElem(key);
                ifElem.setIf(currentElem.getNext());
                if (null != prevElem) {
                    prevElem.setNext(ifElem);
                } else {
                    currentElem = ifElem;
                }
                ifElem.setNext(appendIfBody(currentElem, ifElem, key));
                analyzeIf(null, ifElem.getIf());
                analyzeIf(null, ifElem.getElse());
                currentElem = ifElem;
            }
        }
        if (!currentElem.isLast()) {
            analyzeIf(currentElem, currentElem.getNext());
        }
        return currentElem;
    }

    private static SqlElem appendIfBody(SqlElem prevElem, IfElem parentIf, String parentIfKey) {
        SqlElem currentElem = prevElem.getNext();
        if (currentElem instanceof CommentElem) {
            String comment = ((CommentElem) currentElem).getComment();
            {
                Pattern pattern = Pattern.compile("^ ELSE #(.*)#");
                Matcher matcher = pattern.matcher(comment);
                if (matcher.find()) {
                    String key = matcher.group(1);
                    if (key.equals(parentIfKey)) {
                        parentIf.setElse(currentElem.getNext());
                        prevElem.setNext(EoqElem.INSTANCE);
                        if (currentElem == parentIf.getIf().getNext()) {
                            parentIf.setIf(EoqElem.INSTANCE);
                        }
                        if (!currentElem.isLast()) {
                            return appendElseBody(currentElem, parentIf, parentIfKey);
                        }
                    }
                }
            }
            {
                Pattern pattern = Pattern.compile("^ ENDIF #(.*)#");
                Matcher matcher = pattern.matcher(comment);
                if (matcher.find()) {
                    String key = matcher.group(1);
                    if (key.equals(parentIfKey)) {
                        prevElem.setNext(EoqElem.INSTANCE);
                        return currentElem.getNext();
                    }
                }
            }
        }
        if (!currentElem.isLast()) {
            return appendIfBody(currentElem, parentIf, parentIfKey);
        }
        throw new QueryTemplateException("'-- IF #" + parentIfKey + "#' is not closed.");
    }

    private static SqlElem appendElseBody(SqlElem prevElem, IfElem parentIf, String parentIfKey) {
        SqlElem currentElem = prevElem.getNext();
        if (currentElem instanceof CommentElem) {
            String comment = ((CommentElem) currentElem).getComment();
            Pattern pattern = Pattern.compile("^ ENDIF #(.*)#");
            Matcher matcher = pattern.matcher(comment);
            if (matcher.find()) {
                String key = matcher.group(1);
                if (key.equals(parentIfKey)) {
                    prevElem.setNext(EoqElem.INSTANCE);
                    return currentElem.getNext();
                }
            }

        }
        if (!currentElem.isLast()) {
            return appendElseBody(currentElem, parentIf, parentIfKey);
        }
        throw new QueryTemplateException("'-- IF #" + parentIfKey + "#' is not closed.");
    }

    // ///////////////////////

    private static SqlElem analyzeForeach(SqlElem prevElem, SqlElem currentElem) {
        if (currentElem instanceof CommentElem) {
            String comment = ((CommentElem) currentElem).getComment();
            Pattern pattern = Pattern.compile("^ FOREACH #(.*)#");
            Matcher matcher = pattern.matcher(comment);
            if (matcher.find()) {
                String key = matcher.group(1);
                ForeachElem foreachElem = new ForeachElem(key);
                foreachElem.setBody(currentElem.getNext());
                if (null != prevElem) {
                    prevElem.setNext(foreachElem);
                } else {
                    currentElem = foreachElem;
                }
                foreachElem.setNext(appendForeachBody(currentElem, foreachElem, key));
                if ((null != foreachElem.getBody()) && !(foreachElem.getBody() instanceof EoqElem)) {
                    analyzeForeach(null, foreachElem.getBody());
                }
                currentElem = foreachElem;
            }
        } else if (currentElem instanceof IfElem) {
            IfElem ifElem = (IfElem) currentElem;
            analyzeForeach(null, ifElem.getIf());
            analyzeForeach(null, ifElem.getElse());
        }
        if (!currentElem.isLast()) {
            analyzeForeach(currentElem, currentElem.getNext());
        }
        return currentElem;
    }

    private static SqlElem appendForeachBody(SqlElem prevElem, ForeachElem parentForeach, String parentForeachKey) {
        SqlElem currentElem = prevElem.getNext();
        if (currentElem instanceof CommentElem) {
            String comment = ((CommentElem) currentElem).getComment();
            Pattern pattern = Pattern.compile("^ ENDFOREACH #(.*)#");
            Matcher matcher = pattern.matcher(comment);
            if (matcher.find()) {
                String key = matcher.group(1);
                if (key.equals(parentForeachKey)) {
                    prevElem.setNext(EoqElem.INSTANCE);
                    return currentElem.getNext();
                }
            }
        }
        if (!currentElem.isLast()) {
            return appendForeachBody(currentElem, parentForeach, parentForeachKey);
        }
        throw new QueryTemplateException("'-- FOREACH #" + parentForeachKey + "#' is not closed.");
    }
}
