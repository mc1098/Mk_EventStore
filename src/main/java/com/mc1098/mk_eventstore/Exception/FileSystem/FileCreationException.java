/*
 * Copyright (C) 2019 Max Cripps <43726912+mc1098@users.noreply.github.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mc1098.mk_eventstore.Exception.FileSystem;

import java.io.File;

/**
 *
 * @author Max Cripps <43726912+mc1098@users.noreply.github.com>
 */
public class FileCreationException extends FileSystemException
{
    public FileCreationException(File file, Throwable thwable)
    {
        super(String.format("The File System was unable to create the file at "
                + "path %s.", file.getPath()), thwable);
    }
}
