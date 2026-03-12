package com.msc.cloud.customer_order_api.karate;

import com.intuit.karate.junit5.Karate;


class KarateTestRunner {

    /**
     * Run all Karate tests in the karate folder.
     *
     * This method uses the @Karate.Test annotation which is recognized
     * by JUnit 5 as a test method.
     */
    @Karate.Test
    Karate testAll() {
        return Karate.run()
                .relativeTo(getClass())
                .path("classpath:karate")  // Path to .feature files
                .tags("~@ignore");          // Exclude tests tagged with @ignore
    }

    /**
     * Run only customer-related tests.
     */
    @Karate.Test
    Karate testCustomer() {
        return Karate.run("classpath:karate/customer.feature")
                .relativeTo(getClass());
    }
}

