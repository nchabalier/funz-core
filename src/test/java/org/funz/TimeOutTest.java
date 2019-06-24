package org.funz;

import org.junit.Test;
import org.funz.util.TimeOut;

/**
 *
 * @author richet
 */
public class TimeOutTest {

    long wait = 5000;

    public static void main(String args[]) {
        org.junit.runner.JUnitCore.main(TimeOutTest.class.getName());
    }

    @Test
    public void testResultTooLate() {
        System.err.println("++++++++++++++++++++++++++ testResultTooLate");

        final Object stopp = new Object();
        final Object defaultt = new Object();
        final Object dummy = new Object();
        TimeOut t = new TimeOut("testResultTooLate") {

            volatile boolean stop=false;
            @Override
            protected boolean break_command() {
                System.err.println("break_command");
                stop = true;
                return true;
            }
            
            @Override
            protected Object command() {
                try {
                    for (int i = 0; i < 10; i++) {
                        if (stop) {
                            System.err.println("stop");
                            return stopp;// to check that it was not dummy or null
                        }
                        Thread.sleep(wait/10 * 2);
                        if (i>6) System.err.println( "!!!!!!");
                        System.err.print("-");
                    }
                } catch (InterruptedException ex) {
                    System.err.println( "Well interrupted before return");
                    assert true : "Well interrupted before return";
                    return new Object();// to check that it was not dummy or null
                }
                assert false : "Did not interrupted before return !";
                
                return dummy;
            }

            @Override
            protected Object defaultResult() {
                return defaultt;
            }

        };
        try {
            t.execute(wait);
            assert false : "TimeOut not reached !";
        } catch (TimeOut.TimeOutException ex) {
            assert true : "TimeOut reached !";
        }
        assert t.getResult() != stopp : "Returned stop !!! (while expected to return before)";
        assert t.getResult() != dummy : "Returned result !!! (while expected to be interrupted before)";
        assert t.getResult() == defaultt : "Failed to return null:" + t.getResult();
        
        t.join(); //not needed in true usage, just here to avoid NoJoinError
        
    }

    @Test
    public void testResultBeforeTimeOut() {
        System.err.println("++++++++++++++++++++++++++ testResultBeforeTimeOut");

        final Object dummy = new Object();
        final TimeOut t = new TimeOut("testResultBeforeTimeOut") {

            @Override
            protected Object command() {
                try {
                    Thread.sleep(wait / 3);
                } catch (InterruptedException ex) {
                    assert false : "Interrupted before return !";
                }
                assert true : "Not interrupted before return !";

                return dummy;
            }

            @Override
            protected Object defaultResult() {
                return null;
            }

        };

        Thread tt = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2 * wait / 3);
                } catch (InterruptedException ex) {
                    assert false : "!!!";
                }
                assert t.getResult() == dummy : "Failed to return dummy:" + t.getResult();
            }
        });
        tt.start();

        try {
            t.execute(wait);
        } catch (TimeOut.TimeOutException ex) {
            assert false : "TimeOut reached !";
        }
        
        t.join(); //not needed in true usage, just here to avoid NoJoinError
        try {
            tt.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testSimpleReturn() {
        System.err.println("++++++++++++++++++++++++++ testSimpleReturn");

        final Object dummy = new Object();
        TimeOut t = new TimeOut("testSimpleReturn") {

            @Override
            protected Object command() {
                return dummy;
            }

            @Override
            protected Object defaultResult() {
                return null;
            }

        };
        try {
            t.execute(wait);
        } catch (TimeOut.TimeOutException ex) {
            assert false : "TimeOut reached !";
        }
        assert t.getResult() == dummy : "Failed to return dummy:" + t.getResult();

        t.join();
    }

    @Test
    public void testInterrupt() throws InterruptedException {
        System.err.println("++++++++++++++++++++++++++ testInterrupt");
        final Object dummy = new Object();
        final Object dummydefault = new Object();
        final TimeOut t = new TimeOut("testInterrupt") {
volatile boolean break_it = false;
            @Override
            protected Object command() {
                try {
                    for (int i = 0; i < 15; i++) {
                        if (break_it) return null;
                        System.err.print("x");
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException ex) {
                    System.err.println("Interrupted !");
                    return null;
                }
                System.err.println("Not Interrupted !!!");
                return dummy;
            }

            @Override
            protected boolean break_command() {
            break_it = true;
return true;
            }
            

            @Override
            protected Object defaultResult() {
                return dummydefault;
            }

        };

        Thread background = new Thread(new Runnable() {

            public void run() {
                try {
                    t.execute(10000);
                } catch (TimeOut.TimeOutException ex) {
                    assert true : "TimeOut not reached !";
                }

            }
        }, "Run thread");
        background.start();

        Thread.sleep(2000);

        t.interrupt();

        assert t.getResult() == null : "Failed to interrupt: returned " + (t.getResult() == dummy ? "command" : (t.getResult() == dummydefault ? "default" : "?"));

        t.join();
        background.join();
    }

    @Test
    public void testErrorWhileExecute() {
        System.err.println("++++++++++++++++++++++++++ testErrorWhileExecute");

        final Object default_dummy = new Object();
        final Object dummy = new Object();
        TimeOut t = new TimeOut("testErrorWhileExecute") {

            @Override
            protected Object command() {

                try {
                    throw new Exception("ARRRRRGGGGG");
                    //return dummy;
                } catch (Exception ex) {
                    return null;
                }
            }

            @Override
            protected Object defaultResult() {
                return default_dummy;
            }

        };
        try {
            t.execute(wait);
            assert false : "Was expected to fail before that point !";
        } catch (Exception ex) {

        }
        assert t.getResult() != null : "returned something while exception throw:" + t.getResult();

        t.join();
    }

    @Test
    public void testDefaultReturn() throws InterruptedException {
        System.err.println("++++++++++++++++++++++++++ testDefaultReturn");

        final Object default_dummy = new Object();
        TimeOut t = new TimeOut("testDefaultReturn") {

            @Override
            protected Object command() {
                try {
                    Thread.sleep(wait * 2);
                } catch (InterruptedException ex) {
                }
                return null;
            }

            @Override
            protected Object defaultResult() {
                return default_dummy;
            }

        };
        try {
            t.execute(wait);
        } catch (TimeOut.TimeOutException ex) {
        }
        t.join();

        assert t.getResult() == default_dummy : "Failed to return default_dummy:" + t.getResult();

        Thread.sleep(wait * 2);
    }

}
