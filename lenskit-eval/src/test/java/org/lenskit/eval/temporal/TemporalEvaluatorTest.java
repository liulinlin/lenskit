/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.eval.temporal;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.lenskit.LenskitConfiguration;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.baseline.ItemMeanRatingItemScorer;
import org.lenskit.baseline.UserMeanBaseline;
import org.lenskit.baseline.UserMeanItemScorer;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TemporalEvaluatorTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    public DataAccessObject dao;
    public File predictOutputFile;
    public TemporalEvaluator tempEval = new TemporalEvaluator();

    @Before
    public void initialize() throws IOException {
        predictOutputFile = folder.newFile("predictions.csv");
        List<Rating> ratings;
        ImmutableList.Builder<Rating> bld = ImmutableList.builder();

        bld.add(Rating.create(13, 102, 3.5, 1L))
           .add(Rating.create(13, 105, 3.5, 2L))
           .add(Rating.create(13, 102, 2.5, 1050L))
           .add(Rating.create(13, 111, 4.5, 1050L))
           .add(Rating.create(13, 111, 4.5, 1200L))
           .add(Rating.create(13, 105, 2.5, 1400L))
           .add(Rating.create(13, 120, 4.5, 1650L))
           .add(Rating.create(13, 121, 4.5, 1650L))
           .add(Rating.create(13, 122, 2.5, 1650L))
           .add(Rating.create(13, 123, 2.5, 1650L))
           .add(Rating.create(13, 111, 3.5, 1700L))
           .add(Rating.create(13, 115, 3.5, 1700L))
           .add(Rating.create(13, 105, 3.5, 1700L))
           .add(Rating.create(13, 102, 2.5, 1750L))
           .add(Rating.create(13, 111, 4.5, 1750L))
           .add(Rating.create(13, 121, 4.5, 1800L))
           .add(Rating.create(13, 105, 2.5, 1800L))
           .add(Rating.create(13, 120, 4.5, 1850L))
           .add(Rating.create(13, 121, 4.5, 1850L))
           .add(Rating.create(13, 122, 2.5, 1850L))
           .add(Rating.create(13, 123, 2.5, 1850L))
           .add(Rating.create(13, 111, 3.5, 1900L))
           .add(Rating.create(13, 115, 3.5, 1900L))
           .add(Rating.create(13, 105, 3.5, 1900L))
           .add(Rating.create(13, 102, 2.5, 1950L))
           .add(Rating.create(13, 111, 4.5, 1950L))
           .add(Rating.create(13, 121, 4.5, 2000L))
           .add(Rating.create(13, 105, 2.5, 2400L))
           .add(Rating.create(39, 120, 4.5, 2650L))
           .add(Rating.create(12, 121, 4.5, 2650L))
           .add(Rating.create(42, 122, 2.5, 2650L))
           .add(Rating.create(40, 123, 2.5, 2650L))
           .add(Rating.create(41, 111, 3.5, 2700L))
           .add(Rating.create(42, 115, 3.5, 2700L));

        ratings = bld.build();

        dao = StaticDataSource.fromList(ratings).get();

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(ItemScorer.class).to(UserMeanItemScorer.class);
        config.bind(UserMeanBaseline.class, ItemScorer.class).to(ItemMeanRatingItemScorer.class);

        tempEval.setRebuildPeriod(1L);
        tempEval.setDataSource(dao);
        tempEval.setAlgorithm("UserMeanBaseline", config);
        tempEval.setOutputFile(predictOutputFile);
    }

    @Test
    public void ExecuteTest() throws IOException, RecommenderBuildException {
        tempEval.execute();
        assertTrue(predictOutputFile.isFile());
        try (FileReader reader = new FileReader(predictOutputFile)) {
            try (LineNumberReader lnr = new LineNumberReader(reader)) {
                lnr.skip(Long.MAX_VALUE);
                long lines = (long) lnr.getLineNumber();
                assertThat(lines, equalTo(35L));
            }
        }
    }

    @Test
    public void SetDataSourceDaoTest() throws IOException, RecommenderBuildException {
        tempEval.setDataSource(dao);
        tempEval.execute();
        assertTrue(predictOutputFile.isFile());

        try (FileReader reader = new FileReader(predictOutputFile)) {
            try (LineNumberReader lnr = new LineNumberReader(reader)) {
                lnr.skip(Long.MAX_VALUE);
                long lines = (long) lnr.getLineNumber();
                assertThat(lines, equalTo(35L));
            }
        }
    }
}
