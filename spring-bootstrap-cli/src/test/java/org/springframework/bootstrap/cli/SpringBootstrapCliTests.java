package org.springframework.bootstrap.cli;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.bootstrap.cli.SpringBootstrapCli.NoArgumentsException;
import org.springframework.bootstrap.cli.SpringBootstrapCli.NoHelpCommandArgumentsException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link SpringBootstrapCli}.
 * 
 * @author Phillip Webb
 */
public class SpringBootstrapCliTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private SpringBootstrapCli cli;

	@Mock
	private Command regularCommand;

	@Mock
	private Command optionCommand;

	private Set<Call> calls = EnumSet.noneOf(Call.class);

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.cli = new SpringBootstrapCli() {

			@Override
			protected void showUsage() {
				SpringBootstrapCliTests.this.calls.add(Call.SHOW_USAGE);
				super.showUsage();
			};

			@Override
			protected void errorMessage(String message) {
				SpringBootstrapCliTests.this.calls.add(Call.ERROR_MESSAGE);
				super.errorMessage(message);
			}

			@Override
			protected void printStackTrace(Exception ex) {
				SpringBootstrapCliTests.this.calls.add(Call.PRINT_STACK_TRACE);
				super.printStackTrace(ex);
			}
		};
		given(this.regularCommand.getName()).willReturn("command");
		given(this.regularCommand.getDescription()).willReturn("A regular command");
		given(this.optionCommand.getName()).willReturn("option");
		given(this.optionCommand.getDescription()).willReturn("An optional command");
		given(this.optionCommand.isOptionCommand()).willReturn(true);
		this.cli.setCommands(Arrays.asList(this.regularCommand, this.optionCommand));
	}

	@Test
	public void runWithoutArguments() throws Exception {
		this.thrown.expect(NoArgumentsException.class);
		this.cli.run();
	}

	@Test
	public void runCommand() throws Exception {
		this.cli.run("command", "--arg1", "arg2");
		verify(this.regularCommand).run("--arg1", "arg2");
	}

	@Test
	public void runOptionCommand() throws Exception {
		this.cli.run("--option", "--arg1", "arg2");
		verify(this.optionCommand).run("--arg1", "arg2");
	}

	@Test
	public void runOptionCommandWithoutOption() throws Exception {
		this.cli.run("option", "--arg1", "arg2");
		verify(this.optionCommand).run("--arg1", "arg2");
	}

	@Test
	public void runOptionOnNonOptionCommand() throws Exception {
		this.thrown.expect(NoSuchOptionException.class);
		this.cli.run("--command", "--arg1", "arg2");
	}

	@Test
	public void missingCommand() throws Exception {
		this.thrown.expect(NoSuchCommandException.class);
		this.cli.run("missing");
	}

	@Test
	public void handlesSuccess() throws Exception {
		int status = this.cli.runAndHandleErrors("--option");
		assertThat(status, equalTo(0));
		assertThat(this.calls, equalTo((Set<Call>) EnumSet.noneOf(Call.class)));
	}

	@Test
	public void handlesNoArgumentsException() throws Exception {
		int status = this.cli.runAndHandleErrors();
		assertThat(status, equalTo(1));
		assertThat(this.calls, equalTo((Set<Call>) EnumSet.of(Call.SHOW_USAGE)));
	}

	@Test
	public void handlesNoSuchOptionException() throws Exception {
		int status = this.cli.runAndHandleErrors("--missing");
		assertThat(status, equalTo(1));
		assertThat(this.calls, equalTo((Set<Call>) EnumSet.of(Call.SHOW_USAGE)));
	}

	@Test
	public void handlesRegularException() throws Exception {
		willThrow(new RuntimeException()).given(this.regularCommand).run();
		int status = this.cli.runAndHandleErrors("command");
		assertThat(status, equalTo(1));
		assertThat(this.calls, equalTo((Set<Call>) EnumSet.of(Call.ERROR_MESSAGE)));
	}

	@Test
	public void handlesExceptionWithDashD() throws Exception {
		willThrow(new RuntimeException()).given(this.regularCommand).run();
		int status = this.cli.runAndHandleErrors("command", "-d");
		assertThat(status, equalTo(1));
		assertThat(this.calls, equalTo((Set<Call>) EnumSet.of(Call.ERROR_MESSAGE,
				Call.PRINT_STACK_TRACE)));
	}

	@Test
	public void handlesExceptionWithDashDashDebug() throws Exception {
		willThrow(new RuntimeException()).given(this.regularCommand).run();
		int status = this.cli.runAndHandleErrors("command", "--debug");
		assertThat(status, equalTo(1));
		assertThat(this.calls, equalTo((Set<Call>) EnumSet.of(Call.ERROR_MESSAGE,
				Call.PRINT_STACK_TRACE)));
	}

	@Test
	public void exceptionMessages() throws Exception {
		assertThat(new NoSuchOptionException("name").getMessage(),
				equalTo("Unknown option: --name"));
		assertThat(new NoSuchCommandException("name").getMessage(),
				equalTo("spring: 'name' is not a valid command. See 'spring --help'."));
	}

	@Test
	public void help() throws Exception {
		this.cli.run("help", "command");
		verify(this.regularCommand).printHelp((PrintStream) anyObject());
	}

	@Test
	public void helpNoCommand() throws Exception {
		this.thrown.expect(NoHelpCommandArgumentsException.class);
		this.cli.run("help");
	}

	@Test
	public void helpUnknownCommand() throws Exception {
		this.thrown.expect(NoSuchCommandException.class);
		this.cli.run("help", "missing");
	}

	private static enum Call {
		SHOW_USAGE, ERROR_MESSAGE, PRINT_STACK_TRACE
	}
}
