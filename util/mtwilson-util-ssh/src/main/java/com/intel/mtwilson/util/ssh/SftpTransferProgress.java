/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.ssh;

import com.intel.dcsg.cpg.performance.Progress;
import java.io.IOException;
import net.schmizz.sshj.common.StreamCopier;

/**
 *
 * @author jbuhacoff
 */
public class SftpTransferProgress implements Progress, StreamCopier.Listener {

    private long progress, progressMax;

    public SftpTransferProgress(long size) {
        this.progress = 0;
        this.progressMax = size;
    }

    @Override
    public long getCurrent() {
        return progress;
    }

    @Override
    public long getMax() {
        return progressMax;
    }

    /**
     * Called by SFTPFileTransfer to update how many bytes have been transferred
     *
     * @param transferred
     * @throws IOException
     */
    @Override
    public void reportProgress(long transferred) throws IOException {
        progress = transferred;
    }
}
