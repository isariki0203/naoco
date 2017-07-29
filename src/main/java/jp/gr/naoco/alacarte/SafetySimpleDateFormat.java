package jp.gr.naoco.alacarte;

import java.text.AttributedCharacterIterator;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * スレッド単位で個別のSimpleDateFormatインスタンスを保有することで、スレッドセーフ化したSimpleDateFormat派生クラス
 * <p>
 * クラスのstaticフィールドでSimpleDateFormatを定義した際に、マルチスレッド環境で安全にparse/formatを実行するために、
 * 内部でThreadLocalにSimpleDateFormatインスタンスを格納し、そのインスタンスに対してメソッドを呼び出す。
 * </p>
 * <p>
 * したがって、set...のようなインスタンスの属性を変更するようなメソッドを実行した場合、その影響を受けるのは同一スレッドでのインスタンスの実行のみとなる。
 * </p>
 * 
 * @author naoco0917
 */
public class SafetySimpleDateFormat extends SimpleDateFormat {

    private static final long serialVersionUID = 8475134192884982238L;

    private ConstructorCall constructor_ = null;

    private String pattern_ = null;

    private DateFormatSymbols formatSymbols_ = null;

    private Locale locale_ = null;

    private final ThreadLocal<SimpleDateFormat> INSTANCES = new ThreadLocal<SimpleDateFormat>();

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    public SafetySimpleDateFormat() {
        constructor_ = new ConstructorCall() {
            @Override
            public SimpleDateFormat construct() {
                return new SimpleDateFormat();
            }
        };
    }

    public SafetySimpleDateFormat(String pattern) {
        pattern_ = pattern;
        constructor_ = new ConstructorCall() {
            @Override
            public SimpleDateFormat construct() {
                return new SimpleDateFormat(pattern_);
            }
        };
    }

    public SafetySimpleDateFormat(String pattern, DateFormatSymbols formatSymbols) {
        pattern_ = pattern;
        formatSymbols_ = formatSymbols;
        constructor_ = new ConstructorCall() {
            @Override
            public SimpleDateFormat construct() {
                return new SimpleDateFormat(pattern_, formatSymbols_);
            }
        };
    }

    public SafetySimpleDateFormat(String pattern, Locale locale) {
        pattern_ = pattern;
        locale_ = locale;
        constructor_ = new ConstructorCall() {
            @Override
            public SimpleDateFormat construct() {
                return new SimpleDateFormat(pattern_, locale_);
            }
        };
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden from java.text.Format

    @Override
    public Object parseObject(String source) throws ParseException {
        return instance().parseObject(source);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden from java.text.DateFormat

    @Override
    public Calendar getCalendar() {
        return instance().getCalendar();
    }

    @Override
    public NumberFormat getNumberFormat() {
        return instance().getNumberFormat();
    }

    @Override
    public TimeZone getTimeZone() {
        return instance().getTimeZone();
    }

    @Override
    public boolean isLenient() {
        return instance().isLenient();
    }

    @Override
    public Date parse(String source) throws ParseException {
        return instance().parse(source);
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        return instance().parseObject(source, pos);
    }

    @Override
    public void setCalendar(Calendar newCalendar) {
        instance().setCalendar(newCalendar);
    }

    @Override
    public void setLenient(boolean lenient) {
        instance().setLenient(lenient);
    }

    @Override
    public void setNumberFormat(NumberFormat newNumberFormat) {
        instance().setNumberFormat(newNumberFormat);
    }

    @Override
    public void setTimeZone(TimeZone zone) {
        instance().setTimeZone(zone);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden from java.text.SimpleDateFormat

    @Override
    public void applyLocalizedPattern(String pattern) {
        instance().applyLocalizedPattern(pattern);
    }

    @Override
    public void applyPattern(String pattern) {
        instance().applyPattern(pattern);
    }

    @Override
    public Object clone() {
        return instance().clone();
    }

    @Override
    public boolean equals(Object obj) {
        return instance().equals(obj);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
        return instance().format(date, toAppendTo, pos);
    }

    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        return instance().formatToCharacterIterator(obj);
    }

    @Override
    public Date get2DigitYearStart() {
        return instance().get2DigitYearStart();
    }

    @Override
    public DateFormatSymbols getDateFormatSymbols() {
        return instance().getDateFormatSymbols();
    }

    @Override
    public int hashCode() {
        return instance().hashCode();
    }

    @Override
    public Date parse(String text, ParsePosition pos) {
        return parse(text, pos);
    }

    @Override
    public void set2DigitYearStart(Date startDate) {
        instance().set2DigitYearStart(startDate);
    }

    @Override
    public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols) {
        instance().setDateFormatSymbols(newFormatSymbols);
    }

    @Override
    public String toLocalizedPattern() {
        return instance().toLocalizedPattern();
    }

    @Override
    public String toPattern() {
        return instance().toPattern();
    }

    // ///////////////////////

    private SimpleDateFormat instance() {
        SimpleDateFormat instance = INSTANCES.get();
        if (null != instance) {
            return instance;
        }
        instance = constructor_.construct();
        INSTANCES.set(instance);
        return instance;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes

    private interface ConstructorCall {
        public SimpleDateFormat construct();
    }

}
