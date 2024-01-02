package tech.icey.util;

public interface ManualDispose extends AutoCloseable {
	@Override
	default void close() {
		if (!isManuallyDisposed()) {
			Logger.log_static(
					Logger.Level.ERROR,
					this.getClass().getName(),
					"ManualDispose 在手动释放前被 GC 清理"
			);

			// don't try to dispose it, because it's meant to be manually disposed, thus must follow
			// some kind of ordering or so
		}
	}
	
	boolean isManuallyDisposed();

	void dispose();
}
