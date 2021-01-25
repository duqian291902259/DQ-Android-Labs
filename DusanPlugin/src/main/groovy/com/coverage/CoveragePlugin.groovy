package com.coverage

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class CoveragePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println " ----------------------- CoveragePlugin -----------------------"
        def android = project.extensions.getByType(AppExtension);
        def transform = new CoverageTransform(project);
        android.registerTransform(transform);
    }

}