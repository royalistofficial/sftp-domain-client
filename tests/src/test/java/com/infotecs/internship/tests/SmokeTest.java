package com.infotecs.internship.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SmokeTest {

    @Test
    public void projectSkeletonCompiles() {
        Assert.assertTrue(true, "Скелет проекта собирается и тесты запускаются");
    }
}
