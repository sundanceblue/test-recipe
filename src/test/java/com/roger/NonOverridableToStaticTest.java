package com.roger;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class NonOverridableToStaticTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new NonOverridableToStatic());
    }


    @Test
    void testSinglePublicAndFinalWithInstance() {

        rewriteRun(
            java(
                """
                        package com.roger;

                        public class A {

                            public int x;
                            public String y;

                            public final int goodBye() { y="foo"; return 0; }
                        }
                    """
            )
        );
    }

    @Test
    void testSinglePrivateWithInstance() {

        rewriteRun(
            java(
                """
                        package com.roger;

                        public class A {

                            public int x;
                            public String y;

                            private String hello() { x=1; return ""; }
                        }
                    """
            )
        );
    }

    @Test
    void testSinglePublicWithoutInstance() {

        rewriteRun(
            java(
                """
                        package com.roger;

                        public class A {

                            public int x;
                            public String y;

                            public String hello() { int z=1; return ""; }
                        }
                    """
            )
        );
    }

    @Test
    void testSinglePrivateWithoutInstance() {

        rewriteRun(
            java(
                """
                        public class A {

                            public int x;
                            public String y;

                            private String hello() { int z=1; return ""; }
                        }
                    """,
                """
                        public class A {

                            public int x;
                            public String y;

                            private static String hello() { int z=1; return ""; }
                        }
                    """
            )
        );
    }

    @Test
    void testSinglePublicStatic() {

        rewriteRun(
            java(
                """
                        package com.roger;

                        public class A {

                            public int x;
                            public String y;

                            public static String hello() { int x=1; return ""; }
                        }
                    """
            )
        );
    }
    @Test
    void testSinglePrivateStatic() {

        rewriteRun(
            java(
                """
                        package com.roger;

                        public class A {

                            public int x;
                            public String y;

                            private static String hello() { int x=1; return ""; }
                        }
                    """
            )
        );
    }

    @Test
    void testPrivateAndPublicFinalWithoutInstance() {

        rewriteRun(
            java(
                """
                        package com.roger;

                        public class A {

                            public int x;
                            public String y;

                            private String hello() { int z=1; return ""; }
                            public final int goodBye() { int z=1; return 0; }
                        }
                    """,
                """
                        package com.roger;

                        public class A {

                            public int x;
                            public String y;

                            private static String hello() { int z=1; return ""; }
                            public static final int goodBye() { int z=1; return 0; }
                        }
                    """
            )
        );
    }

    @Test
    void testPrivateNoInstanceVars() {

        rewriteRun(
            java(
                """
                        package com.roger;

                        public class A {

                            private String hello() { int x=1; return ""; }
                        }
                    """,
                """
                        package com.roger;

                        public class A {

                            private static String hello() { int x=1; return ""; }
                        }
                    """
            )
        );
    }

    @Test
    void testPublicAndFinalNoInstanceVars() {

        rewriteRun(
            java(
                """
                        package com.roger;

                        public class A {

                            public final String hello() { int x=1; return ""; }
                        }
                    """,
                """
                        package com.roger;

                        public class A {

                            public static final String hello() { int x=1; return ""; }
                        }
                    """
            )
        );
    }

    @Test
    void testSinglePublicAndFinalWithStaticAccess() {

        rewriteRun(
            java(
                """
                        package com.roger;

                        public class B{
                            public static String a;
                        }

                        public class A {

                            public int x;
                            public String y;

                            public final int goodBye() { B.a="foo"; return 0; }
                        }
                    """,
               """
                        package com.roger;

                        public class B{
                            public static String a;
                        }

                        public class A {

                            public int x;
                            public String y;

                            public static final int goodBye() { B.a="foo"; return 0; }
                        }
                    """
            )
        );
    }

    @Test
    void testSinglePrivateWithStaticAccess() {

        rewriteRun(
            java(
                """
                        package com.roger;

                        public class B{
                            public static String a;
                        }

                        public class A {

                            public int x;
                            public String y;

                            private int goodBye() { B.a="bar"; return 0; }
                        }
                    """,
                """
                         package com.roger;

                         public class B{
                             public static String a;
                         }

                         public class A {

                             public int x;
                             public String y;

                             private static int goodBye() { B.a="bar"; return 0; }
                         }
                     """
            )
        );
    }

    @Test
    void testSinglePrivateWithStaticAccessSingleClass() {

        rewriteRun(
            java(
                """
                        package com.roger;

                        public class A {

                            public int x;
                            public static String y;

                            private int goodBye() { y="bar"; return 0; }
                        }
                    """,
                """
                         package com.roger;

                         public class A {

                             public int x;
                             public static String y;

                             private static int goodBye() { y="bar"; return 0; }
                         }
                     """
            )
        );
    }

    @Test
    void testSinglePublicWithFinalStaticAccessSingleClass() {

        rewriteRun(
            java(
                """
                        package com.roger;

                        public class A {

                            public int x;
                            public static String y;

                            public final int goodBye() { y="bar"; return 0; }
                        }
                    """,
                """
                         package com.roger;

                         public class A {

                             public int x;
                             public static String y;

                             public static final int goodBye() { y="bar"; return 0; }
                         }
                     """
            )
        );
    }

    @Test
    void testSinglePrivateCallingMethod() {

        rewriteRun(
            java(
                """
                        package com.roger;

                        public class A {

                            public int x;
                            public String y;

                            private String hello() { goodbye(); return ""; }
                            private void goodbye(){ System.out.println("goodbye"); }
                        }
                    """,
                """
                        package com.roger;

                        public class A {

                            public int x;
                            public String y;

                            private static String hello() { goodbye(); return ""; }
                            private static void goodbye(){ System.out.println("goodbye"); }
                        }
                    """
            )
        );
    }

    @Test
    void testSinglePrivateCallingMethodWithThisKeyword() {

        rewriteRun(
            java(
                """
                        package com.roger;

                        public class A {

                            public int x;
                            public String y;

                            private String hello() { this.goodbye(); return ""; }
                            private void goodbye(){ System.out.println("goodbye"); }
                        }
                    """,
                """
                        package com.roger;

                        public class A {

                            public int x;
                            public String y;

                            private String hello() { this.goodbye(); return ""; }
                            private static void goodbye(){ System.out.println("goodbye"); }
                        }
                    """
            )
        );
    }

//    @Test
//    void testSinglePrivateCallingPublicMethod() {
//
//        rewriteRun(
//            java(
//                """
//                        package com.roger;
//
//                        public class A {
//
//                            public int x;
//                            public String y;
//
//                            private String hello() { goodbye(); return ""; }
//                            public void goodbye() { System.out.println("goodbye"); }
//                        }
//                    """
//            )
//        );
//    }
}
