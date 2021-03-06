#import <Foundation/Foundation.h>
#import "NodeI.h"
#import "com/himamis/retex/renderer/share/platform/parser/Element.h"
#import "org/w3c/dom/Element.h"

@interface ElementI : NodeI <RXElement>

@property id <OrgW3cDomElement> element;

- (instancetype)initWithElement:(id <OrgW3cDomElement>)element;

@end
