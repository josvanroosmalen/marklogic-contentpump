/*
 * Copyright 2003-2016 MarkLogic Corporation

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.contentpump;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.Counter;

import com.marklogic.mapreduce.MarkLogicCounter;
import com.marklogic.mapreduce.DocumentURIWithSourceInfo;

/**
 * Mapper for import keyed by DocumentURIWithSourceInfo.
 * 
 * @author jchen
 * 
 * @param <VALUE>
 */
public class ImportDocumentMapper<VALUE> extends 
    BaseMapper<DocumentURIWithSourceInfo, VALUE, DocumentURIWithSourceInfo, 
    VALUE> {
    public static final Log LOG = LogFactory.getLog(ImportDocumentMapper.class);
    protected Counter readCount;
    protected Counter attemptedCount;
    
    public void map(DocumentURIWithSourceInfo uri, VALUE fileContent, 
        Context context) throws IOException, InterruptedException {
        synchronized(readCount) {
            readCount.increment(1);
        }
        if (uri.isSkip()) {
            LOG.warn("Skipped record: " + uri);
            return;
        } 
        synchronized(attemptedCount) {
            attemptedCount.increment(1);
        }
        context.write(uri, fileContent);
    }
    
    @Override
    public void setup(Context context) {
        readCount = context.getCounter(
                MarkLogicCounter.INPUT_RECORDS);
        attemptedCount = context.getCounter(
                MarkLogicCounter.OUTPUT_RECORDS);
    }
}
