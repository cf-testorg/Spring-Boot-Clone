class TestCommand implements Runnable {
	def msg
	TestCommand(String msg) {
		this.msg = msg
	}
	void run() {
		org.springframework.cli.command.ScriptCommandTests.executed = true
		println "Hello ${msg}"
	}
}
new TestCommand(args[0])
