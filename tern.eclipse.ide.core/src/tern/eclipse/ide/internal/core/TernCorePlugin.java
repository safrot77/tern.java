/*******************************************************************************
 * Copyright (c) 2013 Angelo ZERR.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:      
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package tern.eclipse.ide.internal.core;

import org.eclipse.core.runtime.Plugin;

public class TernCorePlugin extends Plugin {

	public static final String PLUGIN_ID = "tern.eclipse.ide.core"; //$NON-NLS-1$

	// The shared instance.
	private static TernCorePlugin plugin;

	/**
	 * The constructor.
	 */
	public TernCorePlugin() {
		super();
		plugin = this;
	}

}