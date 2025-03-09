package me.jirigebauer;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RenameCommitAction extends AnAction {
    private static final String ACTION_TITLE = "Rename Commit";
    private static final String NO_PROJECT_ERROR = "No project is open.";
    private static final String NO_REPO_ERROR = "No Git repository found in the project.";
    private static final String NO_MESSAGE_WARNING = "Please provide a valid commit message.";
    private static final String SUCCESS_MESSAGE = "Commit message updated successfully.";
    private static final String FAILURE_MESSAGE = "Failed to amend commit: ";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getProjectOrShowError(e);
        if (project == null) return;

        GitRepository repo = getFirstRepositoryOrShowError(project);
        if (repo == null) return;

        String newMessage = promptForCommitMessage(project);
        if(newMessage == null) return;

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Renaming commit", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                amendCommit(project, repo, newMessage);
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabled(isActionEnabled(project));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private Project getProjectOrShowError(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) Messages.showErrorDialog(NO_PROJECT_ERROR, ACTION_TITLE + " Error");
        return project;
    }

    private GitRepository getFirstRepositoryOrShowError(@NotNull Project project) {
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        List<GitRepository> repositories = repoManager.getRepositories();
        if(repositories.isEmpty()){
            Messages.showErrorDialog(NO_REPO_ERROR, ACTION_TITLE + " Error");
            return null;
        }
        return repositories.get(0);
    }

    private String promptForCommitMessage(@NotNull Project project) {
        String message = Messages.showInputDialog(project, "Enter the new commit message:", ACTION_TITLE,
                Messages.getQuestionIcon());
        if(message == null || message.trim().isEmpty()) {
            Messages.showErrorDialog(project, NO_MESSAGE_WARNING, ACTION_TITLE);
            return null;
        }
        return message.trim();
    }

    private void amendCommit(@NotNull Project project, @NotNull GitRepository repo, @NotNull String newMessage) {
        Git git = ApplicationManager.getApplication().getService(Git.class);
        GitCommandResult result = git.runCommand(() -> {
            GitLineHandler handler = new GitLineHandler(project, repo.getRoot(), GitCommand.COMMIT);
            handler.addParameters("--amend", "-m", newMessage);
            return handler;
        });

        ApplicationManager.getApplication().invokeLater(() -> {
            if (result.success()) Messages.showInfoMessage(project, SUCCESS_MESSAGE, ACTION_TITLE);
            else {
                String errorDetails = result.getErrorOutputAsJoinedString();
                Messages.showErrorDialog(project, FAILURE_MESSAGE + errorDetails, ACTION_TITLE);
            }
        });
    }

    private boolean isActionEnabled(Project project) {
        return project != null && !GitRepositoryManager.getInstance(project).getRepositories().isEmpty();
    }
}
