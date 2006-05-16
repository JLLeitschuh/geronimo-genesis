/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.gshell.util;

/**
 * ???
 *
 * @version $Id: GShellImpl.java 405303 2006-05-09 04:55:39Z jdillon $
 */
public class Arguments
{
    public static String[] shift(final String[] args) {
        return shift(args, 1);
    }

    public static String[] shift(final String[] args, int pos) {
        assert args.length >= pos;

        String[] _args = new String[args.length - pos];
        System.arraycopy(args, pos, _args, 0, _args.length);
        return _args;
    }
}
