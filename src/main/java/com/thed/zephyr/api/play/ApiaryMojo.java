package com.thed.zephyr.api.play;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Created by masudurrahman on 2/13/16.
 */
@Mojo(requiresDependencyResolution = ResolutionScope.RUNTIME, name = "generateApiDocs")
public class ApiaryMojo extends AbstractMojo {

    @Parameter
    private String packageName;

    @Parameter
    private String BASE_PATH;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        RouteParser routeParser = new RouteParser(packageName,BASE_PATH);
        routeParser.generateApiaryDoc();
    }
}
