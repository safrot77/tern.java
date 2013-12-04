package tern.server.nodejs.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NodejsProcess {

	private final File nodejsBaseDir;
	private final File nodejsTernFile;
	private final File projectDir;
	private Integer port;
	private boolean verbose;
	private Process process;
	private Thread processThread;

	private List<NodejsProcessListener> listeners;

	private final Object lock = new Object();

	public NodejsProcess(File nodejsTernBaseDir, File projectDir) {
		this(null, nodejsTernBaseDir, projectDir);
	}

	public NodejsProcess(File nodejsBaseDir, File nodejsTernBaseDir,
			File projectDir) {
		this.nodejsBaseDir = nodejsBaseDir;
		this.nodejsTernFile = getNodejsTernFile(nodejsTernBaseDir);
		this.projectDir = projectDir;
	}

	private File getNodejsTernFile(File nodejsTernBaseDir) {
		return new File(nodejsTernBaseDir, "node_modules/tern/bin/tern");
	}

	public void start() throws IOException, InterruptedException {
		if (isStarted()) {
			throw new IOException("Nodejs tern Server is already started.");
		}
		List<String> commands = createCommands();
		ProcessBuilder builder = new ProcessBuilder(commands);
		builder.redirectErrorStream(true);
		builder.directory(getProjectDir());
		this.process = builder.start();

		processThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {

					Integer port = null;

					String line = null;
					InputStream is = process.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					try {
						while ((line = br.readLine()) != null) {
							if (port == null) {
								if (line.startsWith("Listening on port ")) {
									port = Integer.parseInt(line.substring(
											"Listening on port ".length(),
											line.length()));
									setPort(port);

									synchronized (lock) {
										lock.notifyAll();
									}

									if (listeners != null) {
										for (NodejsProcessListener listener : listeners) {
											listener.onStart(NodejsProcess.this);
										}
									}
								}
							}
							if (listeners != null) {
								for (NodejsProcessListener listener : listeners) {
									listener.onData(NodejsProcess.this, line);
								}
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					process.waitFor();
					if (listeners != null) {
						for (NodejsProcessListener listener : listeners) {
							listener.onStop(NodejsProcess.this);
						}
					}
					kill();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		processThread.setDaemon(true);
		processThread.start();
	}

	protected List<String> createCommands() throws IOException {
		List<String> commands = new LinkedList<String>();
		if (nodejsBaseDir == null) {
			commands.add("node");
		} else {
			commands.add(new File(nodejsBaseDir.getPath(), "node").getPath());
		}
		commands.add(nodejsTernFile.getCanonicalPath());
		Integer port = getPort();
		if (port != null) {
			commands.add("--port");
			commands.add(port.toString());
		}
		if (isVerbose()) {
			commands.add("--verbose");
			commands.add("1");
		}
		return commands;
	}

	public void kill() {
		if (processThread != null) {
			processThread.interrupt();
		}
		if (process != null) {
			process.destroy();
			process = null;
		}
	}

	public Integer getPort() {
		return port;
	}

	public int start(long timeout) throws InterruptedException, IOException {
		if (!isStarted()) {
			start();
		}
		synchronized (lock) {
			lock.wait(timeout);
		}
		return getPort();
	}

	public boolean isStarted() {
		return process != null;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public File getProjectDir() {
		return projectDir;
	}

	public void join() throws InterruptedException {
		if (processThread != null) {
			processThread.join();
		}
	}

	public void addProcessListener(NodejsProcessListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<NodejsProcessListener>();
		}
		listeners.add(listener);
	}

	public void removeProcessListener(NodejsProcessListener listener) {
		if (listeners != null && listener != null) {
			listeners.remove(listener);
		}
	}

}