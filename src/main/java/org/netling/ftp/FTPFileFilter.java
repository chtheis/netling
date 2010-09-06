/*
* Copyright 2010 netling project <http://netling.org>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package org.netling.ftp;

/**
 * Perform filtering on FTPFile entries.
 */
public interface FTPFileFilter {
    /**
     * Checks if an FTPFile entry should be included or not.
     * 
     * @param file entry to be checked for inclusion. May be <code>null</code>.
     * @return <code>true</code> if the file is to be included, <code>false</code> otherwise
     */
    public boolean accept(FTPFile file);
}
