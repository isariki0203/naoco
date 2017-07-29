package jp.gr.naoco.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ //
        LaolCoreFacadeTest01.class, //
        LaolCoreFacadeTest02.class, //
        SingletoneInstanceFactoryTest01.class, //
        ThreadLocalInstanceFactoryTest01.class //
})
public class LaolCoreAllTests {

}
