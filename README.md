#### 1. Name and Short Description

**llm-code-review-maven-plugin** A Maven plugin for automated code review powered by large language models (LLMs).

This plugin integrates LLM-driven code analysis and review directly into your Maven build process.
It allows you to leverage both cloud-based (e.g., OpenAI, Azure OpenAI) and local (e.g., Ollama, LM Studio) language models to automatically review your project’s source code during build or CI/CD stages.
With flexible configuration and support for custom rules, the plugin helps you automate code quality checks, generate review comments, and enforce code standards within your Maven workflow.

#### 2. Key Features

LLM-powered code review in Maven:
Run automated code analysis and review as part of your Maven build, using large language models for intelligent feedback.

* Supports multiple LLM providers:
  Compatible with cloud-based (OpenAI, Azure OpenAI) and local/self-hosted (Ollama, LM Studio, etc.) LLMs.
* Flexible configuration:
   Specify source files, rule sets, LLM provider settings, quotas, proxies, batching, parallel execution, and more.
* Custom rules and settings:
  Use your own rule definitions and configurations to tailor code review to your project’s needs.
* Build failure criteria:
  Configure build failure based on severity, rule violations, or custom thresholds, allowing automated enforcement of code quality standards.
* Statistics and reporting:
  Generate statistics and summary reports on code issues and review results as part of the Maven build output.

#### 3. Architecture and Integration
The **llm-code-review-maven-plugin** is built on top of the llm-codereview-sdk and is designed for seamless integration with Maven projects.

Core components:

* Mojo Entry Point:
  The plugin’s main entry point is the LlmCodeReviewMojo class, which implements Maven’s goal logic and orchestrates the code review workflow.
* Service Layer:
  Encapsulates core functionality such as file collection, rule processing, statistics calculation, and integration with the LLM code review SDK.
* Parameter Models:
  Custom parameter models (prefixed with P*) map Maven configuration into SDK-compatible data structures for review execution.
* Extensible Parsers and Mappers:
  Support for reading rule files in different formats (JSON, XML), mapping user configuration, and managing advanced options.
* Integration flow:
  1. The plugin reads configuration from your Maven project (pom.xml).
  2. It prepares the necessary parameters and invokes the review process via the SDK.
  3. Review results, comments, and statistics are processed and optionally reported or used as criteria for build success/failure.

This modular design enables powerful code review automation without leaving the Maven ecosystem.

#### 4. Quick Start

##### Step 1: Add the Plugin to Your Project

Add the following to the <plugins> section of your pom.xml:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>com.quasarbyte.llm.codereview</groupId>
            <artifactId>llm-code-review-maven-plugin</artifactId>
            <version>0.1.0-SNAPSHOT</version>
            <configuration>
                <reviewParameter>
                    <reviewName>Find critical defects in code</reviewName>
                    <llmChatCompletionConfiguration>
                        <model>qwen3-8b</model>
                    </llmChatCompletionConfiguration>
                    <systemPrompts>
                        <systemPrompt>You are code review assistant.</systemPrompt>
                    </systemPrompts>
                    <reviewPrompts>
                        <reviewPrompt>Please review all these Java files</reviewPrompt>
                        <reviewPrompt>Comment style: Human like, friendly, natural, and professional tone; ideal for PRs, reports, and communication.</reviewPrompt>
                    </reviewPrompts>
                    <targets>
                        <target>
                            <fileGroups>
                                <fileGroup>
                                    <paths>
                                        <path>src/main/**.java</path>
                                    </paths>
                                    <rules>
                                        <rule>
                                            <code>001</code>
                                            <description>Find possible java.lang.ArrayIndexOutOfBoundsException</description>
                                            <severity>critical</severity>
                                        </rule>
                                    </rules>
                                </fileGroup>
                            </fileGroups>
                        </target>
                    </targets>
                </reviewParameter>
                <llmClientConfiguration>
                    <baseUrl>${baseUrl}</baseUrl>
                    <apiKey>demo</apiKey>
                </llmClientConfiguration>
                <buildFailureConfiguration>
                    <warningThreshold>10</warningThreshold>
                    <criticalThreshold>1</criticalThreshold>
                </buildFailureConfiguration>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>llm-code-review</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
##### Step 2: Run the Plugin

Execute the review as part of your Maven build:

```sh
mvn llm-code-review:llm-code-review
```

Review results, warnings, and failures will be displayed in your Maven output according to your configuration.

For more configuration options, see the documentation and example files.

#### 5. Configuration and Advanced Options

The **llm-code-review-maven-plugin** offers extensive configuration to fit a wide range of code review scenarios and project needs.

Key configuration options include:

* LLM Client Settings:
  Configure the LLM provider endpoint, API key, model, and additional parameters using the <llmClientConfiguration> section.
* Review Parameters:
  Define the scope of code review with <reviewParameter>, specifying review prompts, system prompts, file patterns, and custom rules.
* Rules and Severity:
  Add custom rules directly in the configuration. Each rule can include a unique code, description, and severity (such as critical, warning, etc.).
* File and Target Selection:
  Use glob patterns to include or exclude source files and directories for analysis.
* Batching and Parallelism:
  Fine-tune performance and resource usage by adjusting batching and parallel execution parameters.
* Build Failure Configuration:
  Set thresholds for warnings and critical findings to automatically fail builds if code quality standards are not met.
* Proxy and Quota Support:
  Configure network proxies or request quotas as needed for your environment.

#### 6. Usage Examples

You can find practical usage scenarios and example configurations in the test sources and documentation included in this repository.

Typical use cases:

* Reviewing Java source files with custom rules:
Analyze all *.java files in your project and apply custom-defined rules for code review.

* Integrating with various LLM providers:
Easily switch between OpenAI, Azure OpenAI, Ollama, LM Studio, or any other supported provider by adjusting the <llmClientConfiguration>.

* Automated build quality enforcement:
Configure thresholds for warnings and critical findings to enforce code quality gates during your Maven build.

* Custom review prompts and styles:
Use tailored prompts and comment styles for LLM responses to match your team's communication standards.

For more complete examples, consult the src/test/java/, src/test/resources/, and docs/ directories.

#### 7. Compatibility and Requirements
* Java Version:
The minimum required Java version is Java Development Kit 1.8, providing maximum compatibility with legacy projects.

* Maven:
Requires Maven for build and plugin execution.

* Supported Projects:
Works with standard Java Maven projects and is compatible with multi-module builds.

* LLM Providers:
Supports any LLM provider that can be configured via the plugin, including both cloud-based and local/self-hosted solutions.

Please ensure all necessary API credentials and network access are configured for your chosen LLM provider.

#### 8. Testing and Examples
To better understand how the plugin works in real scenarios, explore the provided tests and example files:

* Test sources:
Practical test cases are located in the src/test/java/ directory.

* Example files:
See src/test/resources/ for sample Java files and configuration setups used in testing.

* Documentation:
The docs/ folder includes additional notes, comment style guidelines, and advanced configuration examples.

Reviewing these files can help you get started quickly and demonstrate common integration patterns for different LLM providers and review configurations.

#### 9. License

This project is licensed under the Apache License, Version 2.0.

You are free to use, modify, and distribute the plugin under the terms of this license.
For the complete license text, see the LICENSE file or visit:
https://www.apache.org/licenses/LICENSE-2.0

#### 10. Support and Contact

If you have questions about using or extending **llm-code-review-maven-plugin**, need help with integration, want to discuss custom software development, collaboration, or have any other inquiries, please contact:

* Email: taluyev+llm-code-review@gmail.com
* LinkedIn: linkedin.com/in/taluyev

We welcome feedback, questions, and suggestions.
