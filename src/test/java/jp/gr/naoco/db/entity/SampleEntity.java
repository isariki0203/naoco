package jp.gr.naoco.db.entity;

import java.sql.Timestamp;

import jp.gr.naoco.db.entity.AbstractEntity;
import jp.gr.naoco.db.entity.annotation.Column;
import jp.gr.naoco.db.entity.annotation.Id;
import jp.gr.naoco.db.entity.annotation.Table;

@Table(name = "HOGE_TBL")
public class SampleEntity extends AbstractEntity {
    @Id(name = "COLUMN1")
    private String column1_;

    public String getColumn1() {
        return column1_;
    }

    public void setColumn1(String value) {
        column1_ = value;
        setFieldNameSet_.add("COLUMN1");
    }

    @Id(name = "COLUMN2")
    private long column2_;

    public long getColumn2() {
        return column2_;
    }

    public void setColumn2(long value) {
        column2_ = value;
        setFieldNameSet_.add("COLUMN2");
    }

    @Column(name = "COLUMN3")
    private String column3_;

    public String getColumn3() {
        return column3_;
    }

    public void setColumn3(String value) {
        column3_ = value;
        setFieldNameSet_.add("COLUMN3");
    }

    @Column(name = "COLUMN4")
    private Timestamp column4_;

    public Timestamp getColumn4() {
        return column4_;
    }

    public void setColumn4(Timestamp value) {
        column4_ = value;
        setFieldNameSet_.add("COLUMN4");
    }

    @Id(name = "COLUMN5")
    private Timestamp column5_;

    public Timestamp getColumn5() {
        return column5_;
    }

    public void setColumn5(Timestamp value) {
        column5_ = value;
        setFieldNameSet_.add("COLUMN5");
    }
}
