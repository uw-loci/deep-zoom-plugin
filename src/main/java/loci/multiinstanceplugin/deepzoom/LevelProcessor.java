/*
 * #%L
 * Deep Zoom plugin for ImageJ.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package loci.multiinstanceplugin.deepzoom;

import loci.multiinstanceplugin.AbstractPlugin;
import loci.multiinstanceplugin.ILinkedPlugin;
import loci.multiinstanceplugin.IPlugin;
import loci.multiinstanceplugin.LinkedPlugin;
import loci.deepzoom.plugin.ImageWrapper;
import loci.deepzoom.plugin.annotations.Img;
import loci.deepzoom.plugin.annotations.Input;
import loci.deepzoom.plugin.annotations.Output;

/**
 * Class at the top of the image chain.  Passes image on to CutTilesProcessor and also
 * ScaleInHalfProcessor.  Note that this assumes that the CutTilesProcessor handles the image
 * in a read-only fashion.
 *
 * When we get back from the inital chain to the CutTilesProcessor all of the tiles for this
 * image level will have been cut and written out.
 */
@Input
@Output({ @Img(LevelProcessor.TILE), @Img(LevelProcessor.HALF) })
public class LevelProcessor extends AbstractPlugin implements IPlugin
{
    static final String TILE = "TILE";
    static final String HALF = "HALF";

    public LevelProcessor()
    {
    }

    public void process() {
        ImageWrapper image1 = get();
        ImageWrapper image2 = new ImageWrapper(image1);

        put(TILE, image1);

        int level = ((Integer)image2.getProperties().get(DeepZoomExporter.LEVEL)).intValue();
        if (level > 0) {
            level--;
            image2.getProperties().set(DeepZoomExporter.LEVEL, new Integer(level));

            put(HALF, image2); //TODO we are assuming tile & halve are non-destructive!!!!
        }
    }
}
