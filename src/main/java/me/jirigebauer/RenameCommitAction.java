package me.jirigebauer;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
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
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("No project is open.", "Rename Commit Error");
            return;
        }

        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        List<GitRepository> repositories = repoManager.getRepositories();
        if(repositories.isEmpty()){
            Messages.showErrorDialog("No repository is open.", "Rename Commit Error");
            return;
        }

        GitRepository repo = repositories.get(0);

        String newName = Messages.showInputDialog(project, "Enter the new commit message:",
                "Rename Commit", Messages.getQuestionIcon());
        if(newName == null || newName.trim().isEmpty()) {
            Messages.showWarningDialog(project, "No commit message provided.", "Rename Commit");
            return;
        }

        Git git = ApplicationManager.getApplication().getService(Git.class);
        GitCommandResult result = git.runCommand(() -> {
            GitLineHandler handler = new GitLineHandler(project, repo.getRoot(), GitCommand.COMMIT);
            handler.addParameters("--amend", "-m", newName);
            return handler;
        });

        if(result.success()) Messages.showInfoMessage(project, "Commit message updated successfully.",
                "Rename Commit");
        else Messages.showErrorDialog(project, "Failed to amend commit:" + result.getOutputAsJoinedString(),
                "Rename Commit Error");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        boolean enabled = project != null && !GitRepositoryManager.getInstance(project).getRepositories().isEmpty();
        e.getPresentation().setEnabled(enabled);
    }
}
