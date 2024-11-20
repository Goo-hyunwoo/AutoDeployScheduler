package goo.edu.cicd.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class DeploymentScheduler {

    // FE 및 BE 디렉토리 경로 설정
    private static final String FE_REPO_DIR = "/fe/path";
    private static final String BE_REPO_DIR = "/be/path";

    @Scheduled(fixedRate = 1000 * 60 * 1) // 5분마다 실행
    public void checkAndDeploy() {
        try {
        	/*
            // FE 변경 사항 확인 및 빌드
            if (isRepoUpdated(FE_REPO_DIR)) {
                System.out.println("FE repository updated. Building...");
                buildFE();
            }
        	
            // BE 변경 사항 확인 및 빌드
            if (isRepoUpdated(BE_REPO_DIR)) {
                System.out.println("BE repository updated. Building...");
                buildBE();
            }

            // Docker 이미지 빌드 및 재배포
            deployDocker();
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isRepoUpdated(String repoDir) throws IOException, InterruptedException {
        // 1. git fetch
        executeCommand(repoDir, "git fetch origin master");

        // 2. git status --porcelain: 변경 사항이 있는지 확인
        String status = executeCommand(repoDir, "git status --porcelain");

        if (!status.isBlank()) {
            executeCommand(repoDir, "git pull origin master");
            return true;
        }

        return false; // 변경 사항이 없으면 false 반환
    }

    private void buildFE() throws IOException, InterruptedException {
        // npm install & npm run build
        executeCommand(FE_REPO_DIR, "npm install");
        executeCommand(FE_REPO_DIR, "npm run build");

        // 빌드 결과를 BE static 폴더로 복사
        executeCommand(null, "rm -rf " + BE_REPO_DIR + "/src/main/resources/static/*");
        executeCommand(null, "cp -r " + FE_REPO_DIR + "/build/* " + BE_REPO_DIR + "/src/main/resources/static/");
    }

    private void buildBE() throws IOException, InterruptedException {
        // Maven clean package
        executeCommand(BE_REPO_DIR, "./mvnw clean package -DskipTests");
    }

    private void deployDocker() throws IOException, InterruptedException {
        // Docker 빌드 및 배포
        executeCommand(null, "docker build -t local/your-service:latest " + BE_REPO_DIR);
        executeCommand(null, "docker-compose -f /path/to/docker-compose.yml up -d --build");
    }

    private String executeCommand(String workingDir, String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);

        if (workingDir != null) {
            processBuilder.directory(new java.io.File(workingDir));
        }

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString();
        }
    }
}
