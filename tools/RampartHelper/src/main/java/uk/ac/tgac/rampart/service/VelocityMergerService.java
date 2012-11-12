package uk.ac.tgac.rampart.service;

import java.io.File;
import java.io.IOException;

import org.apache.velocity.VelocityContext;

public interface VelocityMergerService {

	void merge(File template, File context, File output) throws IOException;

	void merge(File template, VelocityContext context, File output) throws IOException;		
}
