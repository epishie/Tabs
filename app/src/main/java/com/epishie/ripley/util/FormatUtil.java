/*
 * Copyright 2016 Epishie
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

package com.epishie.ripley.util;

import android.text.format.DateUtils;

import java.text.DecimalFormat;

public class FormatUtil {
    public static String getScore(int score) {
        if (score <= 0) {
            return "0";
        }
        final String[] units = new String[] { "", "k", "M" };
        int digitGroups = (int) (Math.log10(score) / Math.log10(1000));
        return new DecimalFormat("#,##0.#").format(score/Math.pow(1000, digitGroups))
                + units[digitGroups];
    }

    public static String getTimeElapsed(long startMillis, long endMillis) {
        long diff = endMillis - startMillis;
        if (diff >= DateUtils.YEAR_IN_MILLIS) {
            return (diff / DateUtils.YEAR_IN_MILLIS) + "y";
        }
        if (diff >= DateUtils.WEEK_IN_MILLIS) {
            return (diff / DateUtils.WEEK_IN_MILLIS) + "w";
        }
        if (diff >= DateUtils.DAY_IN_MILLIS) {
            return (diff / DateUtils.DAY_IN_MILLIS) + "d";
        }
        if (diff >= DateUtils.HOUR_IN_MILLIS) {
            return (diff / DateUtils.HOUR_IN_MILLIS) + "h";
        }
        if (diff >= DateUtils.MINUTE_IN_MILLIS) {
            return (diff / DateUtils.MINUTE_IN_MILLIS) + "m";
        }
        return (diff / DateUtils.SECOND_IN_MILLIS) + "s";
    }
}
