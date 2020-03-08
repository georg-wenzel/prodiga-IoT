package uibk.ac.at.prodiga.utils;

import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProdigaThreadPool {

    private static final class InstanceHolder {
        static final ProdigaThreadPool instance = new ProdigaThreadPool();
    }

    private ExecutorService cachedPool = Executors.newCachedThreadPool();
    private ExecutorService fixedPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private ProdigaThreadPool() {
    }

    public static ProdigaThreadPool getInstance() {
        return InstanceHolder.instance;
    }

    public ExecutorService getCachedPool() {
        return getSecurityDelegatePool(cachedPool);
    }

    public ExecutorService getFixedPool() {
        return getSecurityDelegatePool(fixedPool);
    }

    private DelegatingSecurityContextExecutorService getSecurityDelegatePool(ExecutorService ex) {
        SecurityContext context = SecurityContextHolder.getContext();

        return new DelegatingSecurityContextExecutorService(ex, context);
    }
}
