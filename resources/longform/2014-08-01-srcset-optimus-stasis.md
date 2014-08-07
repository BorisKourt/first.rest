<!--
{
:title "Basic img srcset support for Stasis with Optimus"
:connections [clojure,stasis,optimus,srcset]
}
-->

![Swiss](/resources/public/post-assets/switzerland.jpg)

As I work on creating this site I want to document some bits of progress that others will hopefully find useful. 

One of the topics I want to focus on while using a static site generator is end user page speed. Although still in early browser support stages the `srcset` attribute in html `<img>` tags is a great way to help lower the bandwidth that, especially mobile, users will have to incur. (A shim helps fill in support gaps)

At the time of writing the resources section of this site is still in early development so I will list some of the relevant content and people that helped make getting started with stasis possible for a relative novice like myself.

### Result:

![Swiss](/resources/public/post-assets/switzerland.jpg)

If you inspect the above image you should see something similar to:

```html
<img srcset="/post-assets/adccb3cb3896/200-switzerland.jpg 200w, 
	/post-assets/8558d11b0040/400-switzerland.jpg 400w, 
	/post-assets/ef0a980b2de5/600-switzerland.jpg 600w, 
	/post-assets/ad548f984c46/860-switzerland.jpg 860w, 
	/post-assets/0a6fa84978b4/1020-switzerland.jpg 1020w" 
sizes="(min-width: 721px) 60vw, 80vw)" 
alt="Swiss">
```
This is generated from from an unmodified markdown image syntax. 

As you can see both image sizes and paths for aggressive caching are working.

### The code:

