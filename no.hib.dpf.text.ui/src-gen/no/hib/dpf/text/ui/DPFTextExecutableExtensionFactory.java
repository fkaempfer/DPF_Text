/*
 * generated by Xtext
 */
package no.hib.dpf.text.ui;

import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.osgi.framework.Bundle;

import com.google.inject.Injector;

import no.hib.dpf.text.ui.internal.DPFTextActivator;

/**
 * This class was generated. Customizations should only happen in a newly
 * introduced subclass. 
 */
public class DPFTextExecutableExtensionFactory extends AbstractGuiceAwareExecutableExtensionFactory {

	@Override
	protected Bundle getBundle() {
		return DPFTextActivator.getInstance().getBundle();
	}
	
	@Override
	protected Injector getInjector() {
		return DPFTextActivator.getInstance().getInjector(DPFTextActivator.NO_HIB_DPF_TEXT_DPFTEXT);
	}
	
}
